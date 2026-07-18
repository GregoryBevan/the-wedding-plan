package me.elgregos.theweddingplan.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.acceptedMagicLink
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.alreadyAssignedGuests
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.brideFamilyGuest
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.brideFamilyPublicGuest
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.invalidGuests
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.missingGuests
import kotlin.test.Test

class InvitationDtosTest {

    @Test
    fun `should create invitation guest response`() {
        val response = InvitationGuestResponse(
            id = brideFamilyGuest.id,
            firstName = brideFamilyGuest.firstName,
            lastName = brideFamilyGuest.lastName,
            email = brideFamilyGuest.email,
        )

        assertThat(response).isEqualTo(brideFamilyGuest)
    }

    @Test
    fun `should create public invitation guest response`() {
        val response = PublicInvitationGuestResponse(
            id = brideFamilyPublicGuest.id,
            firstName = brideFamilyPublicGuest.firstName,
            lastName = brideFamilyPublicGuest.lastName,
        )

        assertThat(response).isEqualTo(brideFamilyPublicGuest)
    }

    @Test
    fun `should create guest access magic link response`() {
        val response = GuestAccessMagicLinkResponse(
            message = MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE,
        )

        assertThat(response).isEqualTo(acceptedMagicLink)
    }

    @Test
    fun `should create already assigned invitation guests response`() {
        val response = AlreadyAssignedInvitationGuestsResponse(
            message = alreadyAssignedGuests.message,
            guestIds = alreadyAssignedGuests.guestIds,
        )

        assertThat(response).isEqualTo(alreadyAssignedGuests)
    }

    @Test
    fun `should create invalid invitation guests response`() {
        val response = InvalidInvitationGuestsResponse(
            message = invalidGuests.message,
            guestIds = invalidGuests.guestIds,
        )

        assertThat(response).isEqualTo(invalidGuests)
    }

    @Test
    fun `should create missing invitation guests response`() {
        val response = MissingInvitationGuestsResponse(
            message = missingGuests.message,
        )

        assertThat(response).isEqualTo(missingGuests)
    }
}

