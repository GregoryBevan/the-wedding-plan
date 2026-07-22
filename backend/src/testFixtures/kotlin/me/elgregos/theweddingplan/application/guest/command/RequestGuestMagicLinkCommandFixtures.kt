package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.albertEinstein
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.unknownToken

object RequestGuestMagicLinkCommandFixtures {
    val validJaneDoe = RequestGuestMagicLinkCommand(
        invitationAccessToken = bridesMaidInvitation.accessToken,
        guestId = janeDoe.id,
    )

    val unknownInvitationForJaneDoe = RequestGuestMagicLinkCommand(
        invitationAccessToken = InvitationAccessToken.fromStringOrNull(unknownToken)
            ?: error("invalid fixture token"),
        guestId = janeDoe.id,
    )

    val guestNotInInvitation = RequestGuestMagicLinkCommand(
        invitationAccessToken = bridesMaidInvitation.accessToken,
        guestId = albertEinstein.id,
    )
}

