package me.elgregos.theweddingplan.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationGetterTest {

    private lateinit var invitations: Invitations
    private lateinit var invitationGetter: InvitationGetter

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        invitationGetter = InvitationGetter(invitations)
    }

    @Test
    fun `should get existing invitation by id`() {
        every { invitations.findById(brideFamilyInvitation.id) } returns brideFamilyInvitation

        assertThat(invitationGetter.get(brideFamilyInvitation.id)).isEqualTo(brideFamilyInvitation)
    }

    @Test
    fun `should return null when invitation does not exist`() {
        every { invitations.findById(brideFamilyInvitation.id) } returns null

        assertThat(invitationGetter.get(brideFamilyInvitation.id)).isEqualTo(null)
    }
}
