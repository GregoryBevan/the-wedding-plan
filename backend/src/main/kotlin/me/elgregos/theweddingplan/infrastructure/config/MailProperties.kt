package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.mail")
data class MailProperties(
    val from: String = "no-reply@localhost",
    val enabled: Boolean = true,
)

