package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("app.deezer")
data class DeezerProperties(
    val accessToken: String,
    val playlistId: String,
    val baseUrl: String = "https://api.deezer.com",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(3),
)

