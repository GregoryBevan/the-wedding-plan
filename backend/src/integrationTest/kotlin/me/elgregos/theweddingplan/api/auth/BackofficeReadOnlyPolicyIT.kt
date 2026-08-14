package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.guest.request.AddGuestRequestFixtures.charlieDavis
import me.elgregos.theweddingplan.api.guest.request.UpdateGuestRequestFixtures.johnDoeUpdated
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.Test

class BackofficeReadOnlyPolicyIT : AbstractEndpointIntegrationTest() {

    private val readOnlyEmail = "viewer@example.com"
    private val adminEmail = "gregory@example.com"
    private val unlistedEmail = "stranger@example.com"

    @Test
    fun `should allow read-only user to list guests`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.get().uri("/api/guests?page=0&size=1")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow read-only user to get a guest detail`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.get().uri("/api/guests/${johnDoe.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow read-only user to list invitations`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.get().uri("/api/invitations?page=0&size=1")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should forbid read-only user from creating a guest`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(charlieDavis)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from updating a guest`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.put().uri("/api/guests/${johnDoe.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(johnDoeUpdated)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from archiving a guest`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.delete().uri("/api/guests/${johnDoe.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from restoring a guest`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.post().uri("/api/guests/${johnDoe.id}/restoration")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from creating an invitation`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(AddInvitationRequestFixtures.noGuest)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid read-only user from updating an invitation`() {
        val csrf = authenticatedCsrfContext(readOnlyEmail)

        restTestClient.put().uri("/api/invitations/${brideFamilyInvitation.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(UpdateInvitationRequestFixtures.noGuest)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid an authenticated but unlisted user from reading`() {
        val csrf = authenticatedCsrfContext(unlistedEmail)

        restTestClient.get().uri("/api/guests?page=0&size=1")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should forbid an authenticated but unlisted user from mutating`() {
        val csrf = authenticatedCsrfContext(unlistedEmail)

        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(charlieDavis)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should allow unauthenticated CORS preflight without hitting the capability gate`() {
        restTestClient.options().uri("/api/guests")
            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-xsrf-token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should still allow admin to read`() {
        val csrf = authenticatedCsrfContext(adminEmail)

        restTestClient.get().uri("/api/guests?page=0&size=1")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should still allow admin to mutate`() {
        val csrf = authenticatedCsrfContext(adminEmail)

        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                charlieDavis.copy(email = "policy-admin-${java.util.UUID.randomUUID()}@example.com"),
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
    }
}

