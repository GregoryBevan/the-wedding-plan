package me.elgregos.theweddingplan

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType

@TestConfiguration
class TestAuthenticationConfig {

    @Bean
    @Order(0)
    fun testOnlySecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/test/**")
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(XorCsrfTokenRequestAttributeHandler())
            }
            .authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }

    @RestController
    class TestAuthenticationController {
        private val csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()

        @GetMapping("/test/login", produces = [MediaType.TEXT_PLAIN_VALUE])
        fun login(
            @RequestHeader(TEST_USER_EMAIL_HEADER) email: String,
            request: HttpServletRequest,
            response: HttpServletResponse,
            session: HttpSession,
        ): String {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            val principal = DefaultOAuth2User(authorities, mapOf("email" to email), "email")
            val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
            val context = SecurityContextHolder.createEmptyContext().also { it.authentication = authentication }
            SecurityContextHolder.setContext(context)
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)

            val token = csrfTokenRepository.generateToken(request)
            csrfTokenRepository.saveToken(token, request, response)
            return session.id
        }

        @GetMapping("/test/csrf", produces = [MediaType.TEXT_PLAIN_VALUE])
        fun csrf(token: CsrfToken): String = token.token
    }

    companion object {
        const val TEST_USER_EMAIL_HEADER = "X-Test-User-Email"
    }
}
