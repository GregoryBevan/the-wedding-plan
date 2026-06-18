package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertFailure
import assertk.assertions.hasMessage
import assertk.assertions.isNotNull
import assertk.assertions.isInstanceOf
import org.springframework.core.env.StandardEnvironment
import kotlin.test.Test

class AuthConfigurationValidatorTest {

    @Test
    fun `should not enforce prod checks outside prod profile`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("dev") }

        val validator = AuthConfigurationValidator(
            environment = environment,
            authProperties = AuthProperties(
                allowedEmails = emptyList(),
                successRedirectUrl = "http://localhost:5173",
            ),
        )

        assertThat(validator).isNotNull()
    }

    @Test
    fun `should fail in prod when allowed emails are empty`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            AuthConfigurationValidator(
                environment = environment,
                authProperties = AuthProperties(
                    allowedEmails = emptyList(),
                    successRedirectUrl = "https://wedding.example.com",
                ),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.auth.allowed-emails must not be empty in prod profile")
    }

    @Test
    fun `should fail in prod when success redirect is not https`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            AuthConfigurationValidator(
                environment = environment,
                authProperties = AuthProperties(
                    allowedEmails = listOf("gregory@example.com"),
                    successRedirectUrl = "http://localhost:5173",
                ),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.auth.success-redirect-url must be a non-localhost https URL in prod profile")
    }

    @Test
    fun `should pass in prod when configuration is valid`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        val validator = AuthConfigurationValidator(
            environment = environment,
            authProperties = AuthProperties(
                allowedEmails = listOf("gregory@example.com"),
                successRedirectUrl = "https://wedding.example.com",
            ),
        )

        assertThat(validator).isNotNull()
    }
}