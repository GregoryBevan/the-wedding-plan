package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation

object GuestMagicLinkFixtures {
    val bridesMaidToJane = GuestMagicLink(
        invitationId = bridesMaidInvitation.id,
        invitationAccessToken = bridesMaidInvitation.accessToken,
        guestId = janeDoe.id,
        guestFirstName = janeDoe.firstName,
        guestEmail = janeDoe.email,
    )
}
