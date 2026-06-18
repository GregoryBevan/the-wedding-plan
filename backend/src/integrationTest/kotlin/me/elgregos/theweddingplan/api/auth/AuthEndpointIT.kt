package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import org.springframework.http.HttpHeaders
import kotlin.test.Test

class AuthEndpointIT : AbstractEndpointIntegrationTest() {

    @Test
    fun `should logout authenticated user and invalidate session`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.post().uri("/auth/logout")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .exchange()
            .expectStatus().isNoContent

        restTestClient.get().uri("/auth/me")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.authenticated").isEqualTo(false)
            .jsonPath("$.email").doesNotExist()
            .jsonPath("$.authorized").isEqualTo(false)
    }

    @Test
    fun `should allow logout even when already anonymous`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/auth/logout")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .exchange()
            .expectStatus().isNoContent
    }
}
