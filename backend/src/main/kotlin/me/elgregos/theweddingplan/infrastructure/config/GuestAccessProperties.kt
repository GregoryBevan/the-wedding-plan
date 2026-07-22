package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.guest-access")
data class GuestAccessProperties(
    val baseUrl: String = "http://localhost:5174",
)

