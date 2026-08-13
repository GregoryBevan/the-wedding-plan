package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.stereotype.Component

/**
 * Resolves a caller's backoffice [BackofficeRole] and granted [BackofficeCapability] set from the
 * configured email allowlists (`app.auth.admin-emails` and `app.auth.read-only-emails`).
 *
 * Precedence is deterministic and **most-privileged-wins**: an email present in both allowlists resolves
 * to [BackofficeRole.ADMIN], so the read-only tier can never downgrade an admin. This is the single
 * authorization primitive the backoffice enforcement (#179 / #181) and UI guard (#180) build on.
 */
@Component
class BackofficeAuthorization(
    authProperties: AuthProperties,
) {

    /**
     * Role → allowlist, ordered by precedence (most-privileged first). Resolution returns the first
     * matching role, so an email in several allowlists resolves to the most privileged one. Adding a
     * role is a single entry here plus its allowlist on [AuthProperties].
     */
    private val allowlistsByRole: List<Pair<BackofficeRole, EmailAllowlist>> = listOf(
        BackofficeRole.ADMIN to authProperties.adminAllowlist(),
        BackofficeRole.READ_ONLY to authProperties.readOnlyAllowlist(),
    )

    fun roleOf(email: String?): BackofficeRole? =
        allowlistsByRole.firstOrNull { (_, allowlist) -> email in allowlist }?.first

    fun capabilitiesOf(email: String?): Set<BackofficeCapability> =
        roleOf(email)?.capabilities ?: emptySet()

    fun hasCapability(email: String?, capability: BackofficeCapability): Boolean =
        capability in capabilitiesOf(email)
}




