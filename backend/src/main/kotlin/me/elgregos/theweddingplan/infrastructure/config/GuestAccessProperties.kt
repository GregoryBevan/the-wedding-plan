package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.guest-access")
data class GuestAccessProperties(
    val baseUrl: String,
    val guestAreaUrl: String,
    val magicLinkTtlSeconds: Long = 900,
    val guestSessionTtlSeconds: Int = 1800,
    val jwtSecret: String,
    val sessionCookieSecure: Boolean = false,
    val sessionCookieSameSite: String = "Lax",
)

