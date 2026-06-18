package me.elgregos.theweddingplan.infrastructure.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.StandardEnvironment

class CorsConfigurationValidatorTest {

    @Test
    fun `should not enforce prod checks outside prod profile`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("dev") }

        CorsConfigurationValidator(
            environment = environment,
            corsProperties = CorsProperties(
                allowedOrigins = emptyList(),
                allowedOriginPatterns = listOf("*"),
                allowCredentials = true,
            ),
        )
    }

    @Test
    fun `should fail in prod when allowed origins are empty`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(allowedOrigins = emptyList()),
            )
        }
    }

    @Test
    fun `should fail in prod when allowed origins are not https`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(allowedOrigins = listOf("http://localhost:5173")),
            )
        }
    }

    @Test
    fun `should fail in prod when wildcard origin is used with credentials`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(
                    allowedOrigins = listOf("*"),
                    allowCredentials = true,
                ),
            )
        }
    }

    @Test
    fun `should fail in prod when wildcard origin pattern is used with credentials`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        assertThrows<IllegalStateException> {
            CorsConfigurationValidator(
                environment = environment,
                corsProperties = CorsProperties(
                    allowedOrigins = listOf("https://app.example.com"),
                    allowedOriginPatterns = listOf("*"),
                    allowCredentials = true,
                ),
            )
        }
    }

    @Test
    fun `should pass in prod when cors configuration is valid`() {
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }

        CorsConfigurationValidator(
            environment = environment,
            corsProperties = CorsProperties(
                allowedOrigins = listOf("https://app.example.com"),
                allowCredentials = true,
            ),
        )
    }
}
