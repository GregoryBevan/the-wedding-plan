package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId

data class GuestMagicLink(
    val invitationId: InvitationId,
    val invitationAccessToken: InvitationAccessToken,
    val guestId: GuestId,
    val guestEmail: String,
)