package me.elgregos.theweddingplan.api.guest

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.invitation.response.GuestSessionResponse
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

class GuestSessionEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var guestSessionTokens: GuestSessionTokens

    @Test
    fun `should reject session lookup without guest session`() {
        restTestClient.get().uri("/api/guest-access/secured/me")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject session lookup when guest does not belong to invitation`() {
        restTestClient.get().uri("/api/guest-access/secured/me")
            .header(HttpHeaders.COOKIE, guestSessionCookie(johnDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should return the verified guest session`() {
        val response = restTestClient.get().uri("/api/guest-access/secured/me")
            .header(HttpHeaders.COOKIE, guestSessionCookie(janeDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestSessionResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected verified guest session in response body")

        assertThat(response).all {
            prop(GuestSessionResponse::guestId).isEqualTo("${janeDoe.id}")
            prop(GuestSessionResponse::invitationId).isEqualTo("${bridesMaidInvitation.id}")
            prop(GuestSessionResponse::firstName).isEqualTo(janeDoe.firstName)
            prop(GuestSessionResponse::lastName).isEqualTo(janeDoe.lastName)
            prop(GuestSessionResponse::language).isEqualTo(janeDoe.language.name)
        }
    }

    private fun guestSessionCookie(guestId: GuestId): String {
        val token = guestSessionTokens.issue(
            GuestSession(guestId = guestId, invitationId = bridesMaidInvitation.id)
        )

        return "$GUEST_SESSION_COOKIE=$token"
    }
}


