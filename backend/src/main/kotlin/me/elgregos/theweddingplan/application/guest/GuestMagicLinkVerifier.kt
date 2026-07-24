package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.application.guest.result.GuestMagicLinkVerificationResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkAccessToken
import me.elgregos.theweddingplan.domain.guest.repository.GuestMagicLinkTokens
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import me.elgregos.theweddingplan.domain.shared.Dates.nowUtcMillis
import org.springframework.stereotype.Service

@Service
class GuestMagicLinkVerifier(
    private val guestMagicLinkTokens: GuestMagicLinkTokens,
    private val invitations: Invitations,
) {

    fun verify(token: GuestMagicLinkAccessToken): GuestMagicLinkVerificationResult {
        val consumedToken = guestMagicLinkTokens.consumeIfValid(token, nowUtcMillis())
            ?: return GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken
        val invitation = invitations.findById(consumedToken.invitationId)
            ?: return GuestMagicLinkVerificationResult.InvitationNotFound

        return invitation.guests
            .firstOrNull { it.id == consumedToken.guestId }
            ?.let { GuestMagicLinkVerificationResult.Verified(invitation = invitation, guestId = it.id) }
            ?: GuestMagicLinkVerificationResult.GuestNotInInvitation
    }
}

