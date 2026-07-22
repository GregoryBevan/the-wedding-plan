package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.guestNotInInvitation
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.unknownInvitationForJaneDoe
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.validJaneDoe
import me.elgregos.theweddingplan.application.guest.result.RequestGuestMagicLinkResult
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkRequesterTest {

    private lateinit var invitationTokenResolver: InvitationTokenResolver
    private lateinit var guestMagicLinkSender: GuestMagicLinkSender
    private lateinit var guestMagicLinkRequester: GuestMagicLinkRequester

    @BeforeTest
    fun setUp() {
        invitationTokenResolver = mockk()
        guestMagicLinkSender = mockk(relaxed = true)
        guestMagicLinkRequester = GuestMagicLinkRequester(invitationTokenResolver, guestMagicLinkSender)
    }

    @Test
    fun `should send magic link when invitation token and selected guest are valid`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val sentCommand = slot<GuestMagicLink>()
        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkSender.send(capture(sentCommand)) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.Sent)
        assertThat(sentCommand.captured.invitationId).isEqualTo(InvitationFixtures.bridesMaidInvitation.id)
        assertThat(sentCommand.captured.invitationAccessToken).isEqualTo(InvitationFixtures.bridesMaidInvitation.accessToken)
        assertThat(sentCommand.captured.guestId).isEqualTo(GuestFixtures.janeDoe.id)
        assertThat(sentCommand.captured.guestFirstName).isEqualTo(GuestFixtures.janeDoe.firstName)
        assertThat(sentCommand.captured.guestEmail).isEqualTo(GuestFixtures.janeDoe.email)
    }

    @Test
    fun `should not send magic link when invitation token is unknown`() {
        every { invitationTokenResolver.resolve(unknownInvitationForJaneDoe.invitationAccessToken) } returns null

        val result = guestMagicLinkRequester.request(unknownInvitationForJaneDoe)

        verify(exactly = 0) { guestMagicLinkSender.send(any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.InvitationNotFound)
    }

    @Test
    fun `should not send magic link when selected guest does not belong to invitation`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val result = guestMagicLinkRequester.request(guestNotInInvitation)

        verify(exactly = 0) { guestMagicLinkSender.send(any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.GuestNotFound)
    }

    @Test
    fun `should not throw when sender fails for valid request`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation
        every { guestMagicLinkSender.send(any()) } throws IllegalStateException("smtp failure")

        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkSender.send(any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.DeliveryFailed)
    }
}