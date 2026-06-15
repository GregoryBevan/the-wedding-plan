package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = emptyList(),
    var allowedOriginPatterns: List<String> = emptyList(),
    var allowedMethods: List<String> = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
    var allowedHeaders: List<String> = listOf("Authorization", "Content-Type", "Accept", "Origin"),
    var allowCredentials: Boolean = true,
)
