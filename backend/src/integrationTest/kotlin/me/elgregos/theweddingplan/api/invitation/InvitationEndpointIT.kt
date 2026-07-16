package me.elgregos.theweddingplan.api.invitation

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.activeGuest
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.aliceSmith
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.archivedGuest
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.beforeUpdate
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.bobJohnson
import me.elgregos.theweddingplan.api.guest.GuestApiTestHelper.createGuest
import me.elgregos.theweddingplan.api.invitation.InvitationApiTestHelper.createInvitation
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bridesMaidInvitation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import kotlin.test.Test

class InvitationEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should create invitation with multiple active guests`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val firstGuest = createGuest(restTestClient, csrf, aliceSmith)
        val secondGuest = createGuest(restTestClient, csrf, bobJohnson)
        val initialCount = invitationCount()
        val createdInvitation = createInvitation(
            restTestClient, csrf, AddInvitationRequest(
                label = "Family table",
                guestIds = listOf(firstGuest.id, secondGuest.id),
                description = "Main family invitation for the front row table."
            )
        )

        assertThat(createdInvitation).all {
            prop(InvitationResponse::label).isEqualTo("Family table")
            prop(InvitationResponse::description).isEqualTo("Main family invitation for the front row table.")
            prop(InvitationResponse::guestCount).isEqualTo(2)
            assertThat(createdInvitation.guests.map { it.id }.toSet()).isEqualTo(setOf(firstGuest.id, secondGuest.id))
        }

        assertThat(invitationCount()).isEqualTo(initialCount + 1)
    }

    @Test
    fun `should return bad request when invitation includes archived guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val activeGuest = createGuest(restTestClient, csrf, activeGuest)
        val archivedGuest = createGuest(restTestClient, csrf, archivedGuest)

        archiveGuest(archivedGuest.id)

        authenticatedJsonPost(
            csrf = csrf,
            uri = "/api/invitations",
            body = AddInvitationRequest(
                label = "Invalid invitation",
                guestIds = listOf(activeGuest.id, archivedGuest.id),
                description = "Invitation with an archived guest."
            )
        )
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Some guests were not found or are archived.")
            .jsonPath("$.guestIds.length()").isEqualTo(1)
            .jsonPath("$.guestIds[0]").isEqualTo(archivedGuest.id)
    }

    @Test
    fun `should return bad request when invitation has no guests`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        authenticatedJsonPost(
            csrf = csrf,
            uri = "/api/invitations",
            body = AddInvitationRequest(
                label = "No guests",
                guestIds = emptyList(),
                description = "No guests."
            )
        )
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("At least one guest is required.")
    }

    @Test
    fun `should return conflict when invitation includes a guest already assigned to another invitation`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        authenticatedJsonPost(
            csrf = csrf,
            uri = "/api/invitations",
            body = AddInvitationRequest(
                label = "Duplicate guest invitation",
                guestIds = listOf(janeDoe.id.toString()),
                description = "Should fail because guest is already assigned."
            )
        )
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Some guests are already assigned to another invitation.")
            .jsonPath("$.guestIds.length()").isEqualTo(1)
            .jsonPath("$.guestIds[0]").isEqualTo("${janeDoe.id}")
    }

    @Test
    fun `should list invitations with pagination`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.get().uri("/api/invitations?page=0&size=10")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").value<Int> { assertThat(it > 0).isEqualTo(true) }
            .jsonPath("$.items[0].id").exists()
            .jsonPath("$.items[0].accessToken").exists()
            .jsonPath("$.items[0].guestCount").exists()
            .jsonPath("$.items[0].guests.length()").isEqualTo(1)
            .jsonPath("$.items[0].guests[0].id").isEqualTo("${janeDoe.id}")
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10)
    }

    @Test
    fun `should get invitation by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.get().uri("/api/invitations/${bridesMaidInvitation.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("${bridesMaidInvitation.id}")
            .jsonPath("$.accessToken").isEqualTo(bridesMaidInvitation.accessToken.value)
            .jsonPath("$.label").isEqualTo("Bridesmaid")
            .jsonPath("$.guestCount").isEqualTo(1)
            .jsonPath("$.guests.length()").isEqualTo(1)
            .jsonPath("$.guests[0].id").isEqualTo("${janeDoe.id}")
    }

    @Test
    fun `should redirect unauthenticated invitation listing to google login`() {
        restTestClient.get().uri("/api/invitations")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should update invitation label and description`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guest = createGuest(restTestClient, csrf, beforeUpdate)

        val invitation = createInvitation(
            restTestClient, csrf, AddInvitationRequest(
                label = "Family table",
                description = "Initial description",
                guestIds = listOf(guest.id)
            )
        )

        val updated = authenticatedJsonPut(
            csrf = csrf,
            uri = "/api/invitations/${invitation.id}",
            body = UpdateInvitationRequest(
                version = invitation.version,
                label = "Updated table",
                description = "Updated description",
                guestIds = listOf(guest.id),
            )
        )
            .expectStatus().isOk
            .expectBody(InvitationResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected updated invitation in response body")

        assertThat(updated).all {
            prop(InvitationResponse::label).isEqualTo("Updated table")
            prop(InvitationResponse::description).isEqualTo("Updated description")
            prop(InvitationResponse::guestCount).isEqualTo(1)
        }
    }

    @Test
    fun `should return conflict when updating invitation with stale version`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guest = createGuest(restTestClient, csrf, beforeUpdate)

        val invitation = createInvitation(
            restTestClient, csrf, AddInvitationRequest(
                label = "Family table",
                description = "Initial description",
                guestIds = listOf(guest.id)
            )
        )

        authenticatedJsonPut(
            csrf = csrf,
            uri = "/api/invitations/${invitation.id}",
            body = UpdateInvitationRequest(
                version = invitation.version + 1,
                label = "Updated table",
                description = "Updated description",
                guestIds = listOf(guest.id),
            )
        )
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `should return not found when updating unknown invitation id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guest = createGuest(restTestClient, csrf, beforeUpdate)

        authenticatedJsonPut(
            csrf = csrf,
            uri = "/api/invitations/${UUID.randomUUID()}",
            body = UpdateInvitationRequest(
                version = 1,
                label = "Updated table",
                description = "Updated description",
                guestIds = listOf(guest.id),
            )
        )
            .expectStatus().isNotFound
    }

    private fun authenticatedJsonPost(
        csrf: CsrfContext,
        uri: String,
        body: Any,
    ) = restTestClient.post().uri(uri)
        .header(HttpHeaders.COOKIE, csrf.cookies)
        .header("X-XSRF-TOKEN", csrf.csrfToken)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()

    private fun authenticatedJsonPut(
        csrf: CsrfContext,
        uri: String,
        body: Any,
    ) = restTestClient.put().uri(uri)
        .header(HttpHeaders.COOKIE, csrf.cookies)
        .header("X-XSRF-TOKEN", csrf.csrfToken)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()

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

