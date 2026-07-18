package me.elgregos.theweddingplan.application.invitation.command

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation

data class AddInvitationCommand(
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation(guests: Set<Guest>) =
        Invitation(label = label.trim(), description = description.trim(), guests = guests)
}