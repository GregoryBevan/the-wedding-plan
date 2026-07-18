package me.elgregos.theweddingplan.api.invitation.request

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation

object UpdateInvitationRequestFixtures {
    val mixedGuestsWithWhitespace = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "  Mixed guests  ",
        description = "  Mixed guests invitation  ",
        guestIds = listOf(" ${johnDoe.id} ", "", "   ", "${janeDoe.id}", "${johnDoe.id}"),
    )

    val blankLabel = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "   ",
        description = "description",
        guestIds = listOf(johnDoe.id.toString()),
    )

    val malformedGuestId = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "Bride Family",
        description = "description",
        guestIds = listOf(johnDoe.id.toString(), "not-a-uuid"),
    )

    val noGuest = UpdateInvitationRequest(
        version = brideFamilyInvitation.version,
        label = "No guests",
        description = "No guests",
        guestIds = emptyList(),
    )
}

