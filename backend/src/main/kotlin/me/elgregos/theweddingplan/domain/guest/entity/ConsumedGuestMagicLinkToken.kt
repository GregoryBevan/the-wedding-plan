package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId

data class ConsumedGuestMagicLinkToken(
    val invitationId: InvitationId,
    val guestId: GuestId,
)