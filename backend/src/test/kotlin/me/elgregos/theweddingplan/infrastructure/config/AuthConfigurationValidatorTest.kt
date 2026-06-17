package me.elgregos.theweddingplan.infrastructure.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.StandardEnvironment

class AuthConfigurationValidatorTest {

    @Test
    fun `should not enforce prod checks outside prod profile`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("dev") }

        AuthConfigurationValidator(
            environment = environment,
            authProperties = AuthProperties(
                allowedEmails = emptyList(),
                successRedirectUrl = "http://localhost:5173",
            ),
        )
    }

    @Test
    fun `should fail in prod when allowed emails are empty`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            AuthConfigurationValidator(
                environment = environment,
                authProperties = AuthProperties(
                    allowedEmails = emptyList(),
                    successRedirectUrl = "https://wedding.example.com",
                ),
            )
        }
    }

    @Test
    fun `should fail in prod when success redirect is not https`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            AuthConfigurationValidator(
                environment = environment,
                authProperties = AuthProperties(
                    allowedEmails = listOf("gregory@example.com"),
                    successRedirectUrl = "http://localhost:5173",
                ),
            )
        }
    }

    @Test
    fun `should pass in prod when configuration is valid`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        AuthConfigurationValidator(
            environment = environment,
            authProperties = AuthProperties(
                allowedEmails = listOf("gregory@example.com"),
                successRedirectUrl = "https://wedding.example.com",
            ),
        )
    }
}