package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import me.elgregos.theweddingplan.domain.shared.Dates

data class UpdateInvitationCommand(
    val invitationId: InvitationId,
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
