package me.elgregos.theweddingplan.api.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.guest.AddGuestRequest
import me.elgregos.theweddingplan.api.guest.GuestResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID
import kotlin.test.Test

class InvitationEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should create invitation with multiple active guests`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val firstGuest = createGuest(csrf, "Alice")
        val secondGuest = createGuest(csrf, "Bob")
        val initialCount = invitationCount()

        val createdInvitation = restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                AddInvitationRequest(
                    label = "Family table",
                    description = "Main family invitation for the front row table.",
                    guestIds = listOf(firstGuest.id, secondGuest.id),
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(InvitationResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created invitation in response body")

        assertThat(createdInvitation.label).isEqualTo("Family table")
        assertThat(createdInvitation.description).isEqualTo("Main family invitation for the front row table.")
        assertThat(createdInvitation.guestCount).isEqualTo(2)
        assertThat(invitationCount()).isEqualTo(initialCount + 1)
    }

    @Test
    fun `should return bad request when invitation includes archived guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val activeGuest = createGuest(csrf, "Active")
        val archivedGuest = createGuest(csrf, "Archived")

        archiveGuest(archivedGuest.id)

        restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                AddInvitationRequest(
                    label = "Invalid invitation",
                    guestIds = listOf(activeGuest.id, archivedGuest.id),
                    description = "Invitation with an archived guest."
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Some guests were not found or are archived.")
            .jsonPath("$.guestIds.length()").isEqualTo(1)
            .jsonPath("$.guestIds[0]").isEqualTo(archivedGuest.id)
    }

    @Test
    fun `should return bad request when invitation has no guests`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                AddInvitationRequest(
                    label = "No guests",
                    guestIds = emptyList(),
                    description = "No guests."
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("At least one guest is required.")
    }

    @Test
    fun `should list invitations with pagination`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guest = createGuest(csrf, "List")

        restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(AddInvitationRequest(label = "List card", guestIds = listOf(guest.id), description = "list card"))
            .exchange()
            .expectStatus().isCreated

        restTestClient.get().uri("/api/invitations?page=0&size=10")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").value<Int> { assertThat(it > 0).isEqualTo(true) }
            .jsonPath("$.items[0].id").exists()
            .jsonPath("$.items[0].guestCount").exists()
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10)
    }

    @Test
    fun `should get invitation by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guest = createGuest(csrf, "Lookup")

        val createdInvitation = restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(AddInvitationRequest(label = "Lookup invitation", guestIds = listOf(guest.id), description = "lookup invitation"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(InvitationResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created invitation in response body")

        restTestClient.get().uri("/api/invitations/${createdInvitation.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(createdInvitation.id)
            .jsonPath("$.label").isEqualTo("Lookup invitation")
            .jsonPath("$.guestCount").isEqualTo(1)
    }

    @Test
    fun `should redirect unauthenticated invitation listing to google login`() {
        restTestClient.get().uri("/api/invitations")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    private fun createGuest(csrf: CsrfContext, namePrefix: String): GuestResponse =
        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                AddGuestRequest(
                    firstName = "$namePrefix-${UUID.randomUUID()}",
                    lastName = "Guest",
                    email = "${namePrefix.lowercase()}-${UUID.randomUUID()}@example.com",
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created guest in response body")

    private fun archiveGuest(id: String) {
        jdbcTemplate.update(
            """
            update guest
            set version = version + 1,
                update_date = now() at time zone 'utc',
                deletion_date = now() at time zone 'utc'
            where id = ?
            """.trimIndent(),
            UUID.fromString(id)
        )
    }

    private fun invitationCount() =
        jdbcTemplate.queryForObject("select count(*) from invitation", Int::class.java) ?: 0
}

