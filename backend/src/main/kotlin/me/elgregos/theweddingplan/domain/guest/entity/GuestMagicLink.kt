package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import java.time.LocalDateTime

data class GuestMagicLink(
    val invitationId: InvitationId,
    val guestId: GuestId,
    val token: GuestMagicLinkAccessToken = GuestMagicLinkAccessToken(),
    val expiresAt: LocalDateTime
) {
    fun guestAccessPath() = "/guest-access/magic-links/${token.value}"
}
