package me.elgregos.theweddingplan.api.invitation.response

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.friendsInvitation

object InvitationResponseFixtures {
    val brideFamilyGuest = InvitationGuestResponse(
        id = johnDoe.id.toString(),
        firstName = johnDoe.firstName,
        lastName = johnDoe.lastName,
        email = johnDoe.email,
    )

    val brideFamily = InvitationResponse(
        id = brideFamilyInvitation.id.toString(),
        accessToken = brideFamilyInvitation.accessToken.value,
        version = brideFamilyInvitation.version,
        creationDate = brideFamilyInvitation.creationDate.toString(),
        updateDate = brideFamilyInvitation.updateDate.toString(),
        label = brideFamilyInvitation.label,
        description = brideFamilyInvitation.description,
        guests = listOf(brideFamilyGuest),
        guestCount = 1,
    )

    val friends = InvitationResponse(
        id = friendsInvitation.id.toString(),
        accessToken = friendsInvitation.accessToken.value,
        version = friendsInvitation.version,
        creationDate = friendsInvitation.creationDate.toString(),
        updateDate = friendsInvitation.updateDate.toString(),
        label = friendsInvitation.label,
        description = friendsInvitation.description,
        guests = friendsInvitation.guests
            .sortedBy { it.id.toString() }
            .map {
                InvitationGuestResponse(
                    id = it.id.toString(),
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                )
            },
        guestCount = friendsInvitation.guests.size,
    )

    val firstPage = InvitationPageResponse(
        items = listOf(brideFamily, friends),
        page = 0,
        size = 20,
        totalItems = 2,
        totalPages = 1,
    )

    val brideFamilyPublicGuest = PublicInvitationGuestResponse(
        id = johnDoe.id.toString(),
        firstName = johnDoe.firstName,
        lastName = johnDoe.lastName,
    )

    val brideFamilyPublic = PublicInvitationResponse(
        label = brideFamilyInvitation.label,
        description = brideFamilyInvitation.description,
        guests = listOf(brideFamilyPublicGuest),
        guestCount = 1,
    )

    val friendsPublic = PublicInvitationResponse(
        label = friendsInvitation.label,
        description = friendsInvitation.description,
        guests = friendsInvitation.guests
            .sortedBy { it.id.toString() }
            .map {
                PublicInvitationGuestResponse(
                    id = it.id.toString(),
                    firstName = it.firstName,
                    lastName = it.lastName,
                )
            },
        guestCount = friendsInvitation.guests.size,
    )

    val acceptedMagicLink = GuestAccessMagicLinkResponse(
        message = MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE,
    )

    val alreadyAssignedGuests = AlreadyAssignedInvitationGuestsResponse(
        message = "Some guests are already assigned",
        guestIds = listOf(johnDoe.id.toString(), janeDoe.id.toString()),
    )

    val invalidGuests = InvalidInvitationGuestsResponse(
        message = "Some guests are invalid",
        guestIds = listOf(johnDoe.id.toString(), janeDoe.id.toString()),
    )

    val missingGuests = MissingInvitationGuestsResponse(
        message = "At least one guest is required",
    )
}

