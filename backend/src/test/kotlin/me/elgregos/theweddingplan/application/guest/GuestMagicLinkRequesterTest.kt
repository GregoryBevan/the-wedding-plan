package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.guestNotInInvitation
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.unknownInvitationForJaneDoe
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommandFixtures.validJaneDoe
import me.elgregos.theweddingplan.application.guest.result.RequestGuestMagicLinkResult
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.repository.GuestMagicLinkTokens
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures
import me.elgregos.theweddingplan.domain.shared.Dates
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkRequesterTest {

    private lateinit var invitationTokenResolver: InvitationTokenResolver
    private lateinit var guestMagicLinkTokens: GuestMagicLinkTokens
    private lateinit var guestMagicLinkSender: GuestMagicLinkSender
    private lateinit var guestMagicLinkRequester: GuestMagicLinkRequester

    @BeforeTest
    fun setUp() {
        invitationTokenResolver = mockk()
        guestMagicLinkTokens = mockk(relaxed = true)
        guestMagicLinkSender = mockk(relaxed = true)
        guestMagicLinkRequester = GuestMagicLinkRequester(
            invitationTokenResolver = invitationTokenResolver,
            guestMagicLinkTokens = guestMagicLinkTokens,
            guestMagicLinkSender = guestMagicLinkSender,
            guestAccessProperties = GuestAccessProperties(magicLinkTtlSeconds = 900),
        )
    }

    @Test
    fun `should send magic link when invitation token and selected guest are valid`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val guestMagicLink = slot<GuestMagicLink>()
        val guest = slot<Guest>()
        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkTokens.create(any()) }
        verify(exactly = 1) { guestMagicLinkSender.send(capture(guestMagicLink), capture(guest)) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.Sent)
        assertThat(guestMagicLink.captured.invitationId).isEqualTo(InvitationFixtures.bridesMaidInvitation.id)
        assertThat(guestMagicLink.captured.guestId).isEqualTo(GuestFixtures.janeDoe.id)
        assertThat(guest.captured.email).isEqualTo(GuestFixtures.janeDoe.email)
        assertThat(guest.captured.firstName).isEqualTo(GuestFixtures.janeDoe.firstName)
        assertThat(guestMagicLink.captured.token.value.isNotBlank()).isEqualTo(true)
        assertThat(guestMagicLink.captured.expiresAt).isGreaterThan(Dates.nowUtc())
    }

    @Test
    fun `should not send magic link when invitation token is unknown`() {
        every { invitationTokenResolver.resolve(unknownInvitationForJaneDoe.invitationAccessToken) } returns null

        val result = guestMagicLinkRequester.request(unknownInvitationForJaneDoe)

        verify(exactly = 0) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.InvitationNotFound)
    }

    @Test
    fun `should not send magic link when selected guest does not belong to invitation`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation

        val result = guestMagicLinkRequester.request(guestNotInInvitation)

        verify(exactly = 0) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.GuestNotFound)
    }

    @Test
    fun `should not throw when sender fails for valid request`() {
        every { invitationTokenResolver.resolve(InvitationFixtures.bridesMaidInvitation.accessToken) } returns InvitationFixtures.bridesMaidInvitation
        every { guestMagicLinkSender.send(any(), any()) } throws IllegalStateException("smtp failure")

        val result = guestMagicLinkRequester.request(validJaneDoe)

        verify(exactly = 1) { guestMagicLinkSender.send(any(), any()) }
        assertThat(result).isEqualTo(RequestGuestMagicLinkResult.DeliveryFailed)
    }
}