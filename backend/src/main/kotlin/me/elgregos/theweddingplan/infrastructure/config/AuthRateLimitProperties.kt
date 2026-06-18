package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.auth.rate-limit")
data class AuthRateLimitProperties(
    val enabled: Boolean = true,
    val maxRequestsPerWindow: Int = 60,
    val windowSeconds: Long = 60,
)
