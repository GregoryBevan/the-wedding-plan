package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import me.elgregos.theweddingplan.application.guest.GuestSessionAuthorizer
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import me.elgregos.theweddingplan.infrastructure.guest.security.GuestSessionAuthenticationFilter
import java.net.URI

@Configuration
@EnableConfigurationProperties(
    CorsProperties::class,
    AuthProperties::class,
    AuthRateLimitProperties::class,
    MailProperties::class,
    GuestAccessProperties::class,
    GuestProperties::class,
)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    private val authProperties: AuthProperties,
) {

    internal fun resolveSuccessRedirectUrl(): String {
        val allowedOrigins = corsProperties.allowedOrigins
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { it.removeSuffix("/") }
            .toSet()

        val configured = authProperties.successRedirectUrl.trim()

        return configured
            .takeIf(String::isNotBlank)
            ?.takeIf { it.startsWith("/") || it.originOrNull() in allowedOrigins }
            ?: "/"
    }

    private fun String.originOrNull(): String? =
        runCatching { URI(this) }
            .getOrNull()
            ?.takeIf { it.scheme == "http" || it.scheme == "https" }
            ?.let { "${it.scheme}://${it.authority}" }

    @Bean
    @Order(1)
    fun guestSecuredFilterChain(
        http: HttpSecurity,
        guestSessionTokens: GuestSessionTokens,
        guestSessionAuthorizer: GuestSessionAuthorizer,
    ): SecurityFilterChain =
        http
            .securityMatcher("/api/guest-access/secured/**")
            .csrf { it.spa() }
            .cors(Customizer.withDefaults())
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                GuestSessionAuthenticationFilter(guestSessionTokens),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .authorizeHttpRequests { auth ->
                auth.anyRequest().access { authentication, _ ->
                    val principal = authentication.get().principal
                    AuthorizationDecision(
                        principal is GuestSession &&
                            guestSessionAuthorizer.isGuestInInvitation(principal.invitationId, principal.guestId)
                    )
                }
            }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .build()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.spa() }
            .cors(Customizer.withDefaults())
            .headers { headersConfigurer ->
                headersConfigurer.contentTypeOptions(Customizer.withDefaults())
                headersConfigurer.referrerPolicy {
                    it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                }
                headersConfigurer.permissionsPolicyHeader {
                    it.policy("geolocation=(), microphone=(), camera=()")
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/guest-access/**").permitAll()
                    .requestMatchers("/api/**").access { authentication, _ ->
                        val authn = authentication.get()

                        if (!authn.isAuthenticated || authn is AnonymousAuthenticationToken) {
                            throw InsufficientAuthenticationException("Full authentication is required")
                        }

                        val email = (authentication.get().principal as? OAuth2User)
                            ?.getAttribute<String>("email")

                        AuthorizationDecision(authProperties.isAllowed(email))
                    }
                    .requestMatchers(
                        "/assets/**",
                        "/backoffice/**",
                        "/public/**",
                        "/guest-access/**",
                        "/*.html",
                        "/favicon.ico",
                        "/favicon.svg",
                        "/oauth2/**",
                        "/login/**",
                        "/auth/me",
                        "/auth/logout",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl(resolveSuccessRedirectUrl(), true)
            }
            .logout { logout ->
                logout.logoutUrl("/auth/logout")
                    .logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            }
            .build()


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration().apply {
            allowedOrigins = corsProperties.allowedOrigins
            allowedOriginPatterns = corsProperties.allowedOriginPatterns
            allowedMethods = corsProperties.allowedMethods
            allowedHeaders = corsProperties.allowedHeaders
            allowCredentials = corsProperties.allowCredentials
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfiguration)
        }
    }
}
