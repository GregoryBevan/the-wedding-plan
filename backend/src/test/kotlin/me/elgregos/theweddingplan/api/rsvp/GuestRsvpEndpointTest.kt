package me.elgregos.theweddingplan.api.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequest
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpSubmitter
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpGetter
import me.elgregos.theweddingplan.application.rsvp.result.GetGuestRsvpResult
import me.elgregos.theweddingplan.application.rsvp.result.SubmitGuestRsvpResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.infrastructure.guest.security.GuestSessionAuthenticationToken
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestRsvpEndpointTest {

    private val session = GuestSession(guestId = janeDoe.id, invitationId = bridesMaidInvitation.id)

    private lateinit var guestRsvpSubmitter: GuestRsvpSubmitter
    private lateinit var guestRsvpGetter: GuestRsvpGetter
    private lateinit var guestRsvpEndpoint: GuestRsvpEndpoint

    @BeforeTest
    fun setUp() {
        guestRsvpSubmitter = mockk()
        guestRsvpGetter = mockk()
        guestRsvpEndpoint = GuestRsvpEndpoint(guestRsvpSubmitter, guestRsvpGetter)
    }

    @Test
    fun `should reject submission with an invalid attendance`() {
        assertThat(guestRsvpEndpoint.submit(submitRequest("MAYBE")).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should create the rsvp on first submission`() {
        every { guestRsvpSubmitter.submit(any()) } returns SubmitGuestRsvpResult.Created(johnDoeRsvp)

        assertThat(guestRsvpEndpoint.submit(submitRequest("ATTENDING")).statusCode()).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `should return ok when updating an existing submission`() {
        every { guestRsvpSubmitter.submit(any()) } returns SubmitGuestRsvpResult.Updated(johnDoeRsvp)

        assertThat(guestRsvpEndpoint.submit(submitRequest("DECLINED")).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return the rsvp of the session guest`() {
        every { guestRsvpGetter.get(janeDoe.id) } returns GetGuestRsvpResult.Submitted(johnDoeRsvp)

        assertThat(guestRsvpEndpoint.fetch(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return no content when the session guest has no rsvp`() {
        every { guestRsvpGetter.get(janeDoe.id) } returns GetGuestRsvpResult.NotSubmittedYet

        assertThat(guestRsvpEndpoint.fetch(authenticatedRequest()).statusCode()).isEqualTo(HttpStatus.NO_CONTENT)
    }

    private fun submitRequest(attendance: String): ServerRequest = mockk {
        every { principal() } returns Optional.of(GuestSessionAuthenticationToken.authenticated(session))
        every { body(SubmitRsvpRequest::class.java) } returns SubmitRsvpRequest(attendance)
    }

    private fun authenticatedRequest(): ServerRequest = mockk {
        every { principal() } returns Optional.of(GuestSessionAuthenticationToken.authenticated(session))
    }
}

