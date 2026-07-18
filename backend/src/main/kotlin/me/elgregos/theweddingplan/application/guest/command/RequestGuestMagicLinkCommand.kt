package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken

data class RequestGuestMagicLinkCommand(
    val invitationAccessToken: InvitationAccessToken,
    val guestId: GuestId,
)