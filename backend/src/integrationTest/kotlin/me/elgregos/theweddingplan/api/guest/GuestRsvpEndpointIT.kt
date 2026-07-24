package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.infrastructure.guest.security.GUEST_SESSION_COOKIE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.test.Test

class GuestRsvpEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var guestSessionTokens: GuestSessionTokens

    @Test
    fun `should reject rsvp submission without guest session`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject rsvp submission when guest does not belong to invitation`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(johnDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should accept rsvp submission with verified guest session`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
    }

    private fun guestSessionCookie(guestId: GuestId): String {
        val token = guestSessionTokens.issue(
            GuestSession(guestId = guestId, invitationId = bridesMaidInvitation.id)
        )

        return "$GUEST_SESSION_COOKIE=$token"
    }
}


