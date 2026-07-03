package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.invitation.Invitation

data class AddInvitationCommand(
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation(guests: Set<Guest>) = Invitation(label = label.trim(), description = description.trim(), guests = guests)
}
