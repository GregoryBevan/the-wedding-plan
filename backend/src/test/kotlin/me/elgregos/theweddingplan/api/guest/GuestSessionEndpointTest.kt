package me.elgregos.theweddingplan.api.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.GuestSessionResolver
import me.elgregos.theweddingplan.application.guest.result.GuestSessionResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.infrastructure.guest.security.GuestSessionAuthenticationToken
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestSessionEndpointTest {

    private val guestSession = GuestSession(guestId = janeDoe.id, invitationId = bridesMaidInvitation.id)

    private lateinit var guestSessionResolver: GuestSessionResolver
    private lateinit var guestSessionEndpoint: GuestSessionEndpoint

    @BeforeTest
    fun setUp() {
        guestSessionResolver = mockk()
        guestSessionEndpoint = GuestSessionEndpoint(guestSessionResolver)
    }


    @Test
    fun `should return the guest session when it resolves`() {
        every { guestSessionResolver.resolve(guestSession) } returns GuestSessionResult.Resolved(janeDoe)

        assertThat(guestSessionEndpoint.me(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return forbidden when the guest no longer belongs to the invitation`() {
        every { guestSessionResolver.resolve(guestSession) } returns GuestSessionResult.GuestNotInInvitation

        assertThat(guestSessionEndpoint.me(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `should return forbidden when the invitation no longer exists`() {
        every { guestSessionResolver.resolve(guestSession) } returns GuestSessionResult.InvitationNotFound

        assertThat(guestSessionEndpoint.me(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun authenticatedRequest(): ServerRequest = mockk {
        every { principal() } returns Optional.of(GuestSessionAuthenticationToken.authenticated(guestSession))
    }
}

