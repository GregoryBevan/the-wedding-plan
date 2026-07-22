package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.albertEinstein
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.unknownToken
import org.awaitility.Awaitility.await
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertTrue

@TestPropertySource(
    properties = [
        "app.auth.rate-limit.enabled=true",
        "app.auth.rate-limit.max-requests-per-window=5",
        "app.auth.rate-limit.window-seconds=60",
    ]
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GuestAccessMagicLinkEndpointIT : AbstractEndpointIntegrationTest() {

    private val expectedAcceptedMessage = "If the request is valid, you will receive an email shortly."

    @Test
    fun `should send magic-link email through mailpit`() {
        val csrf = csrfContext()

        postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, janeDoe.id.toString())
            .expectStatus().isEqualTo(HttpStatus.ACCEPTED)

        val payload = waitForMailpitMessagePayload()

        assertTrue(payload.contains(janeDoe.email))
        assertTrue(payload.contains(bridesMaidInvitation.accessToken.value))
        assertTrue(payload.contains("/guest-access/${bridesMaidInvitation.accessToken.value}/guests/${janeDoe.id}"))
    }

    @Test
    fun `should return accepted non enumerating response for valid and invalid combinations`() {
        val csrf = csrfContext()

        postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, janeDoe.id.toString())
            .expectStatus().isEqualTo(HttpStatus.ACCEPTED)
            .expectBody()
            .jsonPath("$.message").isEqualTo(expectedAcceptedMessage)

        postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, albertEinstein.id.toString())
            .expectStatus().isEqualTo(HttpStatus.ACCEPTED)
            .expectBody()
            .jsonPath("$.message").isEqualTo(expectedAcceptedMessage)

        val refreshedCsrf = csrfContext()

        postMagicLinkRequest(refreshedCsrf, unknownToken, janeDoe.id.toString())
            .expectStatus().isEqualTo(HttpStatus.ACCEPTED)
            .expectBody()
            .jsonPath("$.message").isEqualTo(expectedAcceptedMessage)
    }

    @Test
    fun `should return bad request for malformed guest id or token`() {
        val csrf = csrfContext()

        postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, "not-a-uuid")
            .expectStatus().isBadRequest

        postMagicLinkRequest(csrf, "not-a-uuid", janeDoe.id.toString())
            .expectStatus().isBadRequest
    }

    @Test
    fun `should rate limit repeated magic link requests`() {
        val csrf = csrfContext()

        repeat(5) {
            postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, janeDoe.id.toString())
                .expectStatus().isEqualTo(HttpStatus.ACCEPTED)
        }

        postMagicLinkRequest(csrf, bridesMaidInvitation.accessToken.value, janeDoe.id.toString())
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader().exists("Retry-After")
    }

    private fun postMagicLinkRequest(csrf: CsrfContext, token: String, guestId: String) =
        restTestClient.post().uri("/guest-access/invitations/$token/guests/$guestId/magic-link-requests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()

    private fun waitForMailpitMessagePayload(): String {
        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${mailpitApiBaseUrl()}/api/v1/messages"))
            .GET()
            .build()

        var payload = ""

        await()
            .atMost(Duration.ofSeconds(3))
            .pollInterval(Duration.ofMillis(200))
            .until {
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                val containsToken = response.statusCode() == 200 && response.body().contains(bridesMaidInvitation.accessToken.value)
                if (containsToken) payload = response.body()
                containsToken
            }

        return payload
    }
}
