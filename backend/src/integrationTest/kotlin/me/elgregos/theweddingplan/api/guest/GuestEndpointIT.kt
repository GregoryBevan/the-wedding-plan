package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.TestAuthenticationConfig.Companion.TEST_USER_EMAIL_HEADER
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.charlieDavis
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class GuestEndpointIT : AbstractEndpointIntegrationTest() {

    private data class CsrfContext(
        val cookies: String,
        val csrfToken: String,
    )

    private fun csrfContext(sessionId: String? = null): CsrfContext {
        val authProbe = restTestClient.get().uri("/test/csrf").apply {
            if (sessionId != null) {
                header(HttpHeaders.COOKIE, "JSESSIONID=$sessionId")
            }
        }
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)

        val allCookies = authProbe.responseHeaders[HttpHeaders.SET_COOKIE].orEmpty()
        val xsrfCookie = allCookies.firstOrNull { it.startsWith("XSRF-TOKEN=") }
            ?: error("Missing XSRF-TOKEN cookie")
        val encodedToken = xsrfCookie.substringAfter("XSRF-TOKEN=").substringBefore(';')
        val xsrfToken = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8)

        val cookies = buildString {
            if (sessionId != null) {
                append("JSESSIONID=$sessionId; ")
            }
            append("XSRF-TOKEN=$encodedToken")
        }

        return CsrfContext(
            cookies = cookies,
            csrfToken = xsrfToken,
        )
    }

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
        val sessionId = restTestClient.post().uri("/test/login")
            .header(TEST_USER_EMAIL_HEADER, "gregory@example.com")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
            ?: error("Missing session id from /test/login")

        val csrf = csrfContext(sessionId)

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
