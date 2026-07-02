package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation

data class AddInvitationCommand(
    val label: String,
    val guestIds: Set<GuestId>,
) {
    fun toInvitation() = Invitation(label = label.trim(), guestIds = guestIds)
}
