package me.elgregos.theweddingplan.infrastructure.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SecurityConfigTest {

    @Test
    fun `should allow relative success redirect url`() {
        val config = securityConfig(successRedirectUrl = "/backoffice")

        assertEquals("/backoffice", config.resolveSuccessRedirectUrl())
    }

    @Test
    fun `should allow absolute success redirect url from allowed cors origins`() {
        val config = securityConfig(
            successRedirectUrl = "https://backoffice.example.com/auth/callback",
            allowedOrigins = listOf("https://backoffice.example.com"),
        )

        assertEquals("https://backoffice.example.com/auth/callback", config.resolveSuccessRedirectUrl())
    }

    @Test
    fun `should fallback to root for absolute success redirect url not in allowed cors origins`() {
        val config = securityConfig(
            successRedirectUrl = "https://evil.example.com/phish",
            allowedOrigins = listOf("https://backoffice.example.com"),
        )

        assertEquals("/", config.resolveSuccessRedirectUrl())
    }

    @Test
    fun `should fallback to root for malformed success redirect url`() {
        val config = securityConfig(successRedirectUrl = "javascript:alert(1)")

        assertEquals("/", config.resolveSuccessRedirectUrl())
    }

    private fun securityConfig(
        successRedirectUrl: String,
        allowedOrigins: List<String> = emptyList(),
    ) = SecurityConfig(
        corsProperties = CorsProperties(allowedOrigins = allowedOrigins),
        authProperties = AuthProperties(
            allowedEmails = listOf("gregory@example.com"),
            successRedirectUrl = successRedirectUrl,
        ),
    )
}