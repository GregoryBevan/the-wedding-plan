package me.elgregos.theweddingplan.infrastructure.config

/**
 * Backoffice authorization roles with the capabilities each one grants.
 *
 * - [ADMIN] — full access: grants both [BackofficeCapability.READ] and [BackofficeCapability.WRITE].
 * - [READ_ONLY] — read (safe) access only: grants [BackofficeCapability.READ].
 *
 * Capabilities are **positive grants**, so an admin always holds a superset of a read-only user (never
 * an empty set). Declaration order is **precedence, most-privileged first** (see [BackofficeAuthorization]).
 * Adding a future role means adding an entry here with the capabilities it grants — no resolver `when`
 * branches to touch.
 */
enum class BackofficeRole(val capabilities: Set<BackofficeCapability>) {
    ADMIN(setOf(BackofficeCapability.READ, BackofficeCapability.WRITE)),
    READ_ONLY(setOf(BackofficeCapability.READ)),
}



