package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.auth")
data class AuthProperties(
    val adminEmails: List<String> = emptyList(),
    val readOnlyEmails: List<String> = emptyList(),
    val successRedirectUrl: String = "http://localhost:5173",
) {
    // Config is immutable at runtime, so normalize once instead of on every /api/** request.
    private val adminAllowlist: EmailAllowlist = EmailAllowlist(adminEmails)

    private val readOnlyAllowlist: EmailAllowlist = EmailAllowlist(readOnlyEmails)

    fun adminAllowlist(): EmailAllowlist = adminAllowlist

    fun readOnlyAllowlist(): EmailAllowlist = readOnlyAllowlist

    // Thin delegates kept for the admin-gate call sites (SecurityConfig, AuthEndpoint, validator).
    fun isAdmin(email: String?): Boolean = email in adminAllowlist

    fun normalizedAdminEmails(): Set<String> = adminAllowlist.values
}
