package me.elgregos.theweddingplan.application.guest

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.application.guest.result.GuestMagicLinkVerificationResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkAccessToken
import me.elgregos.theweddingplan.domain.guest.repository.GuestMagicLinkTokens
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import me.elgregos.theweddingplan.domain.shared.Dates.nowUtcMillis
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GuestMagicLinkVerifier(
    private val guestMagicLinkTokens: GuestMagicLinkTokens,
    private val invitations: Invitations,
) {

    fun verify(token: GuestMagicLinkAccessToken): GuestMagicLinkVerificationResult {
        val consumedToken = guestMagicLinkTokens.consumeIfValid(token, nowUtcMillis())
            ?: run {
                logger.warn { "Magic-link verification failed: invalid, expired or already used token" }
                return GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken
            }
        val invitation = invitations.findById(consumedToken.invitationId)
            ?: run {
                logger.warn { "Magic-link verification failed: invitation not found (invitationId=${consumedToken.invitationId})" }
                return GuestMagicLinkVerificationResult.InvitationNotFound
            }

        return invitation.guests
            .firstOrNull { it.id == consumedToken.guestId }
            ?.let {
                logger.info { "Magic-link verified (invitationId=${invitation.id}, guestId=${it.id})" }
                GuestMagicLinkVerificationResult.Verified(invitation = invitation, guestId = it.id)
            }
            ?: run {
                logger.warn { "Magic-link verification failed: guest not in invitation (invitationId=${invitation.id}, guestId=${consumedToken.guestId})" }
                GuestMagicLinkVerificationResult.GuestNotInInvitation
            }
    }
}

