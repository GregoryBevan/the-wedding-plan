package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertFailure
import assertk.assertions.hasMessage
import assertk.assertions.isNotNull
import assertk.assertions.isInstanceOf
import org.springframework.core.env.StandardEnvironment
import kotlin.test.Test

class CorsConfigurationValidatorTest {

    @Test
    fun `should not enforce prod checks outside prod profile`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("dev") }

        val validator = CorsConfigurationValidator(
            environment = environment,
            corsProperties = CorsProperties(
                allowedOrigins = emptyList(),
                allowedOriginPatterns = listOf("*"),
                allowCredentials = true,
            ),
        )

        assertThat(validator).isNotNull()
    }

    @Test
    fun `should fail in prod when allowed origins are empty`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(allowedOrigins = emptyList()),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.cors.allowed-origins must not be empty in prod profile")
    }

    @Test
    fun `should fail in prod when allowed origins are not https`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(allowedOrigins = listOf("http://localhost:5173")),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.cors.allowed-origins must use https in prod profile")
    }

    @Test
    fun `should fail in prod when wildcard origin is used with credentials`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(
                    allowedOrigins = listOf("*"),
                    allowCredentials = true,
                ),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.cors.allowed-origins must not contain '*' when credentials are enabled")
    }

    @Test
    fun `should fail in prod when wildcard origin pattern is used with credentials`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertFailure {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(
                    allowedOrigins = listOf("https://app.example.com"),
                    allowedOriginPatterns = listOf("*"),
                    allowCredentials = true,
                ),
            )
        }
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("app.cors.allowed-origin-patterns must not contain '*' when credentials are enabled")
    }

    @Test
    fun `should pass in prod when cors configuration is valid`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        val validator = CorsConfigurationValidator(
            environment = environment,
            corsProperties = CorsProperties(
                allowedOrigins = listOf("https://app.example.com"),
                allowCredentials = true,
            ),
        )

        assertThat(validator).isNotNull()
    }
}
