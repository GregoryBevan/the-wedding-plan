package me.elgregos.theweddingplan.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.application.invitation.AddInvitationCommandFixtures.brideFamily
import me.elgregos.theweddingplan.application.invitation.AddInvitationCommandFixtures.missingGuests
import me.elgregos.theweddingplan.application.invitation.AddInvitationCommandFixtures.noGuest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationAdderTest {

    private lateinit var invitations: Invitations
    private lateinit var guests: Guests
    private lateinit var invitationAdder: InvitationAdder

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guests = mockk()
        invitationAdder = InvitationAdder(invitations, guests)
    }

    @Test
    fun `should add invitation when all guests are active`() {
        every { guests.findById(johnDoe.id) } returns johnDoe
        every { invitations.add(any()) } returns brideFamilyInvitation

        assertThat(invitationAdder.add(brideFamily)).isEqualTo(AddInvitationResult.Added(brideFamilyInvitation))
    }

    @Test
    fun `should reject invitation when at least one guest is archived or missing`() {
        every { guests.findById(johnDoe.id) } returns null

        assertThat(invitationAdder.add(brideFamily)).isEqualTo(AddInvitationResult.InvalidGuests(brideFamily.guestIds))
    }

    @Test
    fun `should reject invitation when at least one guest is missing`() {
        missingGuests.guestIds.forEach { missingGuestId -> every { guests.findById(missingGuestId) } returns null }

        assertThat(invitationAdder.add(missingGuests)).isEqualTo(AddInvitationResult.InvalidGuests(missingGuests.guestIds))
    }

    @Test
    fun `should return missing guests when guest list is empty`() {
        assertThat(invitationAdder.add(noGuest)).isEqualTo(AddInvitationResult.MissingGuests)
        verify { guests wasNot Called }
        verify { invitations wasNot Called }
    }
}
