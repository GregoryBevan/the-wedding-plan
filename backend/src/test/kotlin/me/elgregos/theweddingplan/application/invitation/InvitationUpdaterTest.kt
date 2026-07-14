package me.elgregos.theweddingplan.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationCommandFixtures.brideFamily
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationCommandFixtures.missingGuests
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationCommandFixtures.mixedGuests
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationCommandFixtures.noGuest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.friendsInvitation
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationUpdaterTest {

    private lateinit var invitations: Invitations
    private lateinit var guests: Guests
    private lateinit var invitationUpdater: InvitationUpdater

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guests = mockk()
        invitationUpdater = InvitationUpdater(invitations, guests)
    }

    @Test
    fun `should update invitation when all guests are active and available`() {
        val updatedInvitation = brideFamily.toInvitation(brideFamilyInvitation, setOf(johnDoe))
        every { invitations.findById(brideFamily.id) } returns brideFamilyInvitation
        every { guests.findByIds(brideFamily.guestIds) } returns setOf(johnDoe)
        every { invitations.findAssignedGuestIds(brideFamily.guestIds) } returns setOf(johnDoe.id)
        every { invitations.update(any()) } returns updatedInvitation

        assertThat(invitationUpdater.update(brideFamily)).isEqualTo(UpdateInvitationResult.Updated(updatedInvitation))
    }

    @Test
    fun `should reject update when invitation does not exist`() {
        every { invitations.findById(brideFamily.id) } returns null

        assertThat(invitationUpdater.update(brideFamily)).isEqualTo(UpdateInvitationResult.NotFound)
        verify { guests wasNot Called }
        verify(exactly = 0) { invitations.update(any()) }
    }

    @Test
    fun `should reject update when at least one guest is already assigned to another invitation`() {
        every { invitations.findById(mixedGuests.id) } returns brideFamilyInvitation
        every { guests.findByIds(mixedGuests.guestIds) } returns setOf(johnDoe, janeDoe)
        every { invitations.findAssignedGuestIds(mixedGuests.guestIds) } returns mixedGuests.guestIds

        assertThat(invitationUpdater.update(mixedGuests)).isEqualTo(
            UpdateInvitationResult.AlreadyAssignedGuests(
                setOf(
                    janeDoe.id
                )
            )
        )
        verify(exactly = 0) { invitations.update(any()) }
    }

    @Test
    fun `should return already assigned guests when update fails due to concurrent unique constraint`() {
        every { invitations.findById(mixedGuests.id) } returns brideFamilyInvitation
        every { guests.findByIds(mixedGuests.guestIds) } returns setOf(johnDoe, janeDoe)
        every { invitations.findAssignedGuestIds(mixedGuests.guestIds) } returnsMany listOf(
            setOf(johnDoe.id),
            mixedGuests.guestIds
        )
        every { invitations.update(any()) } throws DataIntegrityViolationException("duplicate key")

        assertThat(invitationUpdater.update(mixedGuests)).isEqualTo(
            UpdateInvitationResult.AlreadyAssignedGuests(
                setOf(
                    janeDoe.id
                )
            )
        )
    }

    @Test
    fun `should reject update when at least one guest is archived or missing`() {
        every { invitations.findById(brideFamily.id) } returns brideFamilyInvitation
        every { guests.findByIds(brideFamily.guestIds) } returns emptySet()

        assertThat(invitationUpdater.update(brideFamily)).isEqualTo(UpdateInvitationResult.InvalidGuests(brideFamily.guestIds))
    }

    @Test
    fun `should reject update when at least one guest is missing`() {
        every { invitations.findById(missingGuests.id) } returns brideFamilyInvitation
        every { guests.findByIds(missingGuests.guestIds) } returns emptySet()

        assertThat(invitationUpdater.update(missingGuests)).isEqualTo(UpdateInvitationResult.InvalidGuests(missingGuests.guestIds))
    }

    @Test
    fun `should return missing guests when guest list is empty`() {
        every { invitations.findById(noGuest.id) } returns friendsInvitation

        assertThat(invitationUpdater.update(noGuest)).isEqualTo(UpdateInvitationResult.MissingGuests)
        verify { guests wasNot Called }
        verify(exactly = 0) { invitations.update(any()) }
    }

    @Test
    fun `should return not found when invitation disappears during update`() {
        val command = brideFamily
        every { invitations.findById(command.id) } returns brideFamilyInvitation
        every { guests.findByIds(command.guestIds) } returns setOf(johnDoe)
        every { invitations.findAssignedGuestIds(command.guestIds) } returns setOf(johnDoe.id)
        every { invitations.update(any()) } returns null

        assertThat(invitationUpdater.update(command)).isEqualTo(UpdateInvitationResult.NotFound)
    }
}
