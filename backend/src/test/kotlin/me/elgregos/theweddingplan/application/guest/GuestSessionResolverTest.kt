package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.result.GuestSessionResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.albertEinstein
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.nonExistingInvitationId
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestSessionResolverTest {

    private lateinit var invitations: Invitations
    private lateinit var guestSessionResolver: GuestSessionResolver

    @BeforeTest
    fun setUp() {
        invitations = mockk()
        guestSessionResolver = GuestSessionResolver(invitations)
    }

    @Test
    fun `should resolve the guest belonging to the invitation`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestSessionResolver.resolve(GuestSession(guestId = janeDoe.id, invitationId = bridesMaidInvitation.id))

        assertThat(result).isEqualTo(GuestSessionResult.Resolved(janeDoe))
    }

    @Test
    fun `should report guest not in invitation when the guest does not belong to it`() {
        every { invitations.findById(bridesMaidInvitation.id) } returns bridesMaidInvitation

        val result = guestSessionResolver.resolve(GuestSession(guestId = albertEinstein.id, invitationId = bridesMaidInvitation.id))

        assertThat(result).isEqualTo(GuestSessionResult.GuestNotInInvitation)
    }

    @Test
    fun `should report invitation not found when the invitation is missing`() {
        every { invitations.findById(nonExistingInvitationId) } returns null

        val result = guestSessionResolver.resolve(GuestSession(guestId = janeDoe.id, invitationId = nonExistingInvitationId))

        assertThat(result).isEqualTo(GuestSessionResult.InvitationNotFound)
    }
}

