package me.elgregos.theweddingplan.api.invitation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId

data class UpdateInvitationRequest(
    @field:PositiveOrZero
    val version: Long,
    @field:NotBlank
    val label: String,
    val description: String,
    val guestIds: List<String>,
) {

    internal fun toCommandOrNull(id: InvitationId): UpdateInvitationCommand? {
        val parsedGuestIds = guestIds
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
            .toSet()

        return UpdateInvitationCommand(
            id = id,
            version = version,
            label = label.trim(),
            description = description.trim(),
            guestIds = parsedGuestIds,
        )
    }
}