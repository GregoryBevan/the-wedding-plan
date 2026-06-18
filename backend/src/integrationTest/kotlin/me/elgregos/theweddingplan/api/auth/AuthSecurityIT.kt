package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import kotlin.test.Test

class AuthSecurityIT : AbstractEndpointIntegrationTest() {

    @Test
    fun `should allow unauthenticated access to auth status endpoint`() {
        restTestClient.get().uri("/auth/me")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("X-Content-Type-Options")
            .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
            .expectHeader().valueEquals("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
    }

    @Test
    fun `should reject unauthenticated access to protected api endpoint`() {
        val csrf = csrfContext()

        restTestClient.get().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }
}
