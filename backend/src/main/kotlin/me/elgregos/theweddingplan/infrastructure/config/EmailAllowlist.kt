package me.elgregos.theweddingplan.infrastructure.config

/**
 * A case-insensitive, whitespace-trimming set of email addresses.
 *
 * Encapsulates the normalization and membership rules **once** so every tier (admin, read-only, and any
 * future role) behaves identically — adding a new allowlist never re-implements this logic.
 */
class EmailAllowlist(rawEmails: List<String>) {

    val values: Set<String> = rawEmails
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .toSet()

    operator fun contains(email: String?): Boolean =
        email?.let { it.trim().lowercase() in values } ?: false
}

