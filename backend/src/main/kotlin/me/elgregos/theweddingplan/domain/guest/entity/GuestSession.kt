package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId

data class GuestSession(
    val guestId: GuestId,
    val invitationId: InvitationId,
)

