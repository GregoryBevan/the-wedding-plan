package me.elgregos.theweddingplan

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.http.MediaType

@TestConfiguration
class TestAuthenticationConfig {

    @Bean
    @Order(0)
    fun testOnlySecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/test/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }

    @Bean
    fun testAuthenticationFilter(): OncePerRequestFilter = object : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
        ) {
            val email = request.getHeader(TEST_USER_EMAIL_HEADER)?.trim()

            if (!email.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val principal = DefaultOAuth2User(authorities, mapOf("email" to email), "email")
                val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
                val context = SecurityContextHolder.createEmptyContext().also { it.authentication = authentication }
                SecurityContextHolder.setContext(context)
                request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            }

            filterChain.doFilter(request, response)
        }
    }

    @Bean
    fun testAuthenticationFilterRegistration(testAuthenticationFilter: OncePerRequestFilter): FilterRegistrationBean<OncePerRequestFilter> =
        FilterRegistrationBean(testAuthenticationFilter).apply {
            order = Ordered.HIGHEST_PRECEDENCE
        }

    @RestController
    class TestAuthenticationController {
        private val csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()

        @PostMapping("/test/login", produces = [MediaType.TEXT_PLAIN_VALUE])
        fun login(
            @RequestHeader(TEST_USER_EMAIL_HEADER) email: String,
            request: HttpServletRequest,
            response: HttpServletResponse,
            session: HttpSession,
        ): String {
            val token = csrfTokenRepository.generateToken(request)
            csrfTokenRepository.saveToken(token, request, response)
            return session.id
        }

        @GetMapping("/test/csrf", produces = [MediaType.TEXT_PLAIN_VALUE])
        fun csrf(request: HttpServletRequest, response: HttpServletResponse): String {
            val token = csrfTokenRepository.generateToken(request)
            csrfTokenRepository.saveToken(token, request, response)
            return token.token
        }
    }

    companion object {
        const val TEST_USER_EMAIL_HEADER = "X-Test-User-Email"
    }
}
