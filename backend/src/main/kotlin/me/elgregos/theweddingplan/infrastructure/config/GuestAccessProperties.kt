package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.guest-access")
data class GuestAccessProperties(
    val baseUrl: String = "http://localhost:8080",
    val guestAreaUrl: String = "http://localhost:5174/guest-access/secured-area",
    val magicLinkTtlSeconds: Long = 900,
    val guestSessionTtlSeconds: Int = 1800,
)

