package me.elgregos.theweddingplan.api.invitation.request

import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId

data class UpdateInvitationRequest(
    val version: Long,
    val label: String,
    val description: String,
    val guestIds: List<String>,
) {

    internal fun toCommandOrNull(id: InvitationId): UpdateInvitationCommand? {
        val normalizedLabel = label.trim()
        if (normalizedLabel.isEmpty()) return null

        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return UpdateInvitationCommand(
            id = id,
            version = version,
            label = normalizedLabel,
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}