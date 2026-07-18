package me.elgregos.theweddingplan.api.invitation.request

import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId

data class AddInvitationRequest(
    val label: String,
    val description: String,
    val guestIds: List<String>,
){
    internal fun toCommandOrNull(): AddInvitationCommand? {
        val normalizedLabel = label.trim()
        if (normalizedLabel.isEmpty()) return null

        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return AddInvitationCommand(
            label = normalizedLabel,
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}