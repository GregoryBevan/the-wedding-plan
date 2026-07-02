package me.elgregos.theweddingplan.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.application.invitation.AddInvitationCommandFixtures.brideFamily
import me.elgregos.theweddingplan.application.invitation.AddInvitationCommandFixtures.missingGuests
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
        val command = brideFamily

        every { guests.findById(johnDoe.id) } returns johnDoe
        every { invitations.add(any()) } returns brideFamilyInvitation

        assertThat(invitationAdder.add(command)).isEqualTo(AddInvitationResult.Added(brideFamilyInvitation))
    }

    @Test
    fun `should reject invitation when at least one guest is archived or missing`() {
        val command = brideFamily

        every { guests.findById(johnDoe.id) } returns null

        assertThat(invitationAdder.add(command)).isEqualTo(AddInvitationResult.InvalidGuests(command.guestIds))
    }

    @Test
    fun `should reject invitation when at least one guest is missing`() {
        val command = missingGuests

        command.guestIds.forEach { missingGuestId -> every { guests.findById(missingGuestId) } returns null }

        assertThat(invitationAdder.add(command)).isEqualTo(AddInvitationResult.InvalidGuests(command.guestIds))
    }

    @Test
    fun `should return missing guests when guest list is empty`() {
        val command = AddInvitationCommand(label = "No guests", guestIds = emptySet())

        assertThat(invitationAdder.add(command)).isEqualTo(AddInvitationResult.MissingGuests)
        verify { guests wasNot Called }
        verify { invitations wasNot Called }
    }
}
