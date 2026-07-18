package me.elgregos.theweddingplan.application.invitation.command

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import me.elgregos.theweddingplan.domain.shared.Dates

data class UpdateInvitationCommand(
    val id: InvitationId,
    val version: Long,
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation(existing: Invitation, guests: Set<Guest>) =
        existing.copy(
            version = existing.version + 1,
            updateDate = Dates.nowUtcMillis(),
            label = label.trim(),
            description = description.trim(),
            guests = guests,
        )
}