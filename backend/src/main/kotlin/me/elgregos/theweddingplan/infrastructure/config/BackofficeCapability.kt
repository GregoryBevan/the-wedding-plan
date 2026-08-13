package me.elgregos.theweddingplan.infrastructure.config

/**
 * Authoritative backoffice authorization capabilities — the **positive** actions a caller may perform,
 * shared by every module (no per-module permissions).
 *
 * - [READ] (`backoffice.read`) — view backoffice resources (safe HTTP methods).
 * - [WRITE] (`backoffice.write`) — create / update / delete backoffice resources (mutating methods).
 *
 * The read-only role grants [READ] only; admins grant both, so an admin always holds a superset of a
 * read-only user. Enforcement (#179 / #181) requires [WRITE] for mutations, and the UI guard (#180)
 * hides write affordances when [WRITE] is absent.
 *
 * The [id] is the stable string form used when a capability is surfaced outside the JVM (e.g. in the
 * `/auth/me` payload) and must not change without coordinating consumers.
 */
enum class BackofficeCapability(val id: String) {
    READ("backoffice.read"),
    WRITE("backoffice.write"),
}


