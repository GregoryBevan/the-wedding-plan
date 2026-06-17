package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableConfigurationProperties(CorsProperties::class, AuthProperties::class)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    private val authProperties: AuthProperties,
) {

    private fun resolveSuccessRedirectUrl(): String {
        val configured = authProperties.successRedirectUrl.trim()
        return if (
            configured.startsWith("/") ||
            configured.startsWith("http://") ||
            configured.startsWith("https://")
        ) {
            configured
        } else {
            "/"
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
            }
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/guests/**").access { authentication, _ ->
                        val authn = authentication.get()

                        if (!authn.isAuthenticated || authn is AnonymousAuthenticationToken) {
                            throw InsufficientAuthenticationException("Full authentication is required")
                        }

                        val email = (authentication.get().principal as? OAuth2User)
                            ?.getAttribute<String>("email")

                        AuthorizationDecision(authProperties.isAllowed(email))
                    }
                    .requestMatchers("/oauth2/**", "/login/**", "/auth/me", "/error").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl(resolveSuccessRedirectUrl(), true)
            }

        return http.build()
    }

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
