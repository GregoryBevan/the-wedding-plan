package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.charlieDavis
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class GuestEndpointIT : AbstractEndpointIntegrationTest() {

    @Test
    fun `should allow CORS preflight for backoffice origin`() {
        restTestClient.options().uri("/guests")
            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
    }

    @Test
    fun `should redirect unauthenticated guest creation to google login`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(charlieDavis)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should add a new guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.post().uri("/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(charlieDavis)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.version").isEqualTo(1)
            .jsonPath("$.creationDate").isNotEmpty()
            .jsonPath("$.updateDate").isNotEmpty()
            .jsonPath("$.firstName").isEqualTo(charlieDavis.firstName)
            .jsonPath("$.lastName").isEqualTo(charlieDavis.lastName)
            .jsonPath("$.email").isEqualTo(charlieDavis.email)
    }
}
