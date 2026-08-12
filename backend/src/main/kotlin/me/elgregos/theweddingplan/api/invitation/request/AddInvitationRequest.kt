package me.elgregos.theweddingplan.api.invitation.request

import jakarta.validation.constraints.NotBlank
import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId

data class AddInvitationRequest(
    @field:NotBlank
    val label: String,
    val description: String,
    val guestIds: List<String>,
){
    internal fun toCommandOrNull(): AddInvitationCommand? {
        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return AddInvitationCommand(
            label = label.trim(),
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}