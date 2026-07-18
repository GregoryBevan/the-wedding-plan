package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
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
        val command = RequestGuestMagicLinkCommand(
            invitationAccessToken = InvitationFixtures.bridesMaidInvitation.accessToken,
            guestId = GuestFixtures.janeDoe.id,
        )

        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val sentCommand = slot<GuestMagicLink>()

        guestMagicLinkRequester.request(command)

        verify(exactly = 1) { guestMagicLinkSender.send(capture(sentCommand)) }
        assertThat(sentCommand.captured.invitationId).isEqualTo(InvitationFixtures.bridesMaidInvitation.id)
        assertThat(sentCommand.captured.invitationAccessToken).isEqualTo(InvitationFixtures.bridesMaidInvitation.accessToken)
        assertThat(sentCommand.captured.guestId).isEqualTo(GuestFixtures.janeDoe.id)
        assertThat(sentCommand.captured.guestEmail).isEqualTo(GuestFixtures.janeDoe.email)
    }

    @Test
    fun `should not send magic link when invitation token is unknown`() {
        val unknownToken = InvitationAccessToken.fromStringOrNull("b7fb2f39-fa8f-4b8d-b47f-f65bc5fd0ad1")
            ?: error("invalid fixture token")
        val command = RequestGuestMagicLinkCommand(
            invitationAccessToken = unknownToken,
            guestId = GuestFixtures.janeDoe.id,
        )

        every { invitationTokenResolver.resolve(unknownToken) } returns null

        guestMagicLinkRequester.request(command)

        verify(exactly = 0) { guestMagicLinkSender.send(any()) }
    }

    @Test
    fun `should not send magic link when selected guest does not belong to invitation`() {
        val command = RequestGuestMagicLinkCommand(
            invitationAccessToken = InvitationFixtures.bridesMaidInvitation.accessToken,
            guestId = GuestFixtures.albertEinstein.id,
        )

        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        guestMagicLinkRequester.request(command)

        verify(exactly = 0) { guestMagicLinkSender.send(any()) }
    }
}