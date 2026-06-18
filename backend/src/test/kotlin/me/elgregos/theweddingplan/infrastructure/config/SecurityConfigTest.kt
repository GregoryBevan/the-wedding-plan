package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SecurityConfigTest {

    @Test
    fun `should allow relative success redirect url`() {
        val config = securityConfig(successRedirectUrl = "/backoffice")

        assertThat(config.resolveSuccessRedirectUrl()).isEqualTo("/backoffice")
    }

    @Test
    fun `should allow absolute success redirect url from allowed cors origins`() {
        val config = securityConfig(
            successRedirectUrl = "https://backoffice.example.com/auth/callback",
            allowedOrigins = listOf("https://backoffice.example.com"),
        )

        assertThat(config.resolveSuccessRedirectUrl()).isEqualTo("https://backoffice.example.com/auth/callback")
    }

    @Test
    fun `should fallback to root for absolute success redirect url not in allowed cors origins`() {
        val config = securityConfig(
            successRedirectUrl = "https://evil.example.com/phish",
            allowedOrigins = listOf("https://backoffice.example.com"),
        )

        assertThat(config.resolveSuccessRedirectUrl()).isEqualTo("/")
    }

    @Test
    fun `should fallback to root for malformed success redirect url`() {
        val config = securityConfig(successRedirectUrl = "javascript:alert(1)")

        assertThat(config.resolveSuccessRedirectUrl()).isEqualTo("/")
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