package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.auth")
data class AuthProperties(
    val allowedEmails: List<String> = emptyList(),
    val successRedirectUrl: String = "http://localhost:5173",
) {
    fun normalizedAllowedEmails(): Set<String> = allowedEmails
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .toSet()

    fun isAllowed(email: String?): Boolean = email
        ?.trim()
        ?.lowercase()
        ?.let { it in normalizedAllowedEmails() }
        ?: false
}
