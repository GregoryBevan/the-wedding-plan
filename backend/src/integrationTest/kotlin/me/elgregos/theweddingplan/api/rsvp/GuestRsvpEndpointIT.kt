package me.elgregos.theweddingplan.api.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.rsvp.response.GuestRsvpResponse
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
import org.springframework.test.context.jdbc.Sql
import kotlin.test.Test

@Sql(statements = ["DELETE FROM guest_rsvp"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
            .body(mapOf("attendance" to "ATTENDING"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject rsvp retrieval without guest session`() {
        restTestClient.get().uri("/api/guest-access/secured/rsvp")
            .accept(MediaType.APPLICATION_JSON)
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
            .body(mapOf("attendance" to "ATTENDING"))
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should reject rsvp retrieval when guest does not belong to invitation`() {
        restTestClient.get().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, guestSessionCookie(johnDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should reject rsvp submission with an invalid attendance`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "MAYBE"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return no content when the guest has no rsvp yet`() {
        restTestClient.get().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, guestSessionCookie(janeDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `should create the rsvp on first submission`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "ATTENDING", "meal" to "MEAT"))
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `should return ok when updating an existing submission`() {
        submitAttendance(janeDoe.id, "ATTENDING")

        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "DECLINED"))
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should submit then return the guest rsvp`() {
        submitAttendance(janeDoe.id, "ATTENDING")

        assertThat(fetchRsvp(janeDoe.id).attendance).isEqualTo("ATTENDING")
    }

    @Test
    fun `should update the attendance when submitting twice`() {
        submitAttendance(janeDoe.id, "ATTENDING")
        submitAttendance(janeDoe.id, "DECLINED")

        assertThat(fetchRsvp(janeDoe.id).version).isEqualTo(2L)
    }

    @Test
    fun `should ignore a body-supplied guest id and write the session guest rsvp`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "DECLINED", "guestId" to "${johnDoe.id}"))
            .exchange()
            .expectStatus().isCreated

        assertThat(fetchRsvp(janeDoe.id).attendance).isEqualTo("DECLINED")
    }

    @Test
    fun `should reject an attending submission without a meal`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "ATTENDING"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject an attending submission with an unknown meal`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "ATTENDING", "meal" to "PIZZA"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should reject an attending submission with an invalid song payload`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "ATTENDING", "meal" to "MEAT", "song" to mapOf("title" to "Orphans")))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should persist the meal and song for an attending guest`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "ATTENDING", "meal" to "FISH", "song" to songBody))
            .exchange()
            .expectStatus().isCreated

        val rsvp = fetchRsvp(janeDoe.id)
        assertThat(rsvp.meal).isEqualTo("FISH")
        assertThat(rsvp.song?.deezerId).isEqualTo(3135556L)
    }

    @Test
    fun `should ignore choices when the guest declines`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(janeDoe.id)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("attendance" to "DECLINED", "meal" to "MEAT", "song" to songBody))
            .exchange()
            .expectStatus().isCreated

        assertThat(fetchRsvp(janeDoe.id).meal).isNull()
    }

    private val songBody = mapOf(
        "deezerId" to 3135556,
        "title" to "La Vie en rose",
        "artist" to "Édith Piaf",
        "link" to "https://www.deezer.com/track/3135556",
        "preview" to "https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3",
    )

    private fun submitAttendance(guestId: GuestId, attendance: String) {
        val csrf = csrfContext()
        val body = buildMap<String, Any> {
            put("attendance", attendance)
            if (attendance == "ATTENDING") put("meal", "MEAT")
        }

        restTestClient.post().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, "${csrf.cookies}; ${guestSessionCookie(guestId)}")
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().is2xxSuccessful
    }

    private fun fetchRsvp(guestId: GuestId): GuestRsvpResponse =
        restTestClient.get().uri("/api/guest-access/secured/rsvp")
            .header(HttpHeaders.COOKIE, guestSessionCookie(guestId))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestRsvpResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected rsvp in response body")

    private fun guestSessionCookie(guestId: GuestId): String {
        val token = guestSessionTokens.issue(
            GuestSession(guestId = guestId, invitationId = bridesMaidInvitation.id)
        )

        return "$GUEST_SESSION_COOKIE=$token"
    }
}


