package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class CorsConfigurationValidator(
    environment: Environment,
    corsProperties: CorsProperties,
) {

    init {
        if (environment.activeProfiles.contains("prod")) {
            if (corsProperties.allowCredentials) {
                check(corsProperties.allowedOrigins.none { it.trim() == "*" }) {
                    "app.cors.allowed-origins must not contain '*' when credentials are enabled"
                }
                check(corsProperties.allowedOriginPatterns.none { it.trim() == "*" }) {
                    "app.cors.allowed-origin-patterns must not contain '*' when credentials are enabled"
                }
            }

            val normalizedOrigins = corsProperties.allowedOrigins
                .map { it.trim() }
                .filter { it.isNotBlank() }

            check(normalizedOrigins.isNotEmpty()) {
                "app.cors.allowed-origins must not be empty in prod profile"
            }

            check(normalizedOrigins.all { it.startsWith("https://") }) {
                "app.cors.allowed-origins must use https in prod profile"
            }
        }
    }
}
