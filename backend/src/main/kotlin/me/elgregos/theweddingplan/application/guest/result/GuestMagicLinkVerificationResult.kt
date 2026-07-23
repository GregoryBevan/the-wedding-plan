package me.elgregos.theweddingplan.application.guest.result

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation

sealed interface GuestMagicLinkVerificationResult {

    data class Verified(
        val invitation: Invitation,
        val guestId: GuestId,
    ) : GuestMagicLinkVerificationResult

    data object InvalidOrExpiredOrUsedToken : GuestMagicLinkVerificationResult

    data object InvitationNotFound : GuestMagicLinkVerificationResult

    data object GuestNotInInvitation : GuestMagicLinkVerificationResult
}

