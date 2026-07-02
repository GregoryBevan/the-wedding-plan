package me.elgregos.theweddingplan.application.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.InvitationPage
import me.elgregos.theweddingplan.domain.invitation.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationListerTest {

    private lateinit var invitations: Invitations
    private lateinit var invitationLister: InvitationLister

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        invitationLister = InvitationLister(invitations)
    }

    @Test
    fun `should list invitations with provided criteria`() {
        val criteria = InvitationListCriteria(page = 1, size = 5)
        val expectedPage = InvitationPage(
            items = listOf(brideFamilyInvitation),
            page = 1,
            size = 5,
            totalItems = 1,
            totalPages = 1,
        )

        every { invitations.list(criteria) } returns expectedPage

        assertThat(invitationLister.list(criteria)).isEqualTo(expectedPage)
    }

    @Test
    fun `should list invitations with default criteria`() {
        val defaultCriteria = InvitationListCriteria()
        val expectedPage = InvitationPage(
            items = emptyList(),
            page = 0,
            size = 20,
            totalItems = 0,
            totalPages = 0,
        )

        every { invitations.list(defaultCriteria) } returns expectedPage

        assertThat(invitationLister.list(defaultCriteria)).isEqualTo(expectedPage)
    }
}
