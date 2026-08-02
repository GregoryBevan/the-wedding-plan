package me.elgregos.theweddingplan

import me.elgregos.theweddingplan.TestAuthenticationConfig.Companion.TEST_USER_EMAIL_HEADER
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.infrastructure.guest.security.GUEST_SESSION_COOKIE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AbstractEndpointIntegrationTest : AbstractIntegrationTest() {
    data class CsrfContext(
        val cookies: String,
        val csrfToken: String,
    )

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var guestSessionTokens: GuestSessionTokens

    protected val restTestClient: RestTestClient
        get() = RestTestClient.bindToServer().baseUrl("http://localhost:$port").build()

    protected fun csrfContext(sessionId: String? = null): CsrfContext {
        val authProbe = restTestClient.get().uri("/test/csrf").apply {
            if (sessionId != null) {
                header(HttpHeaders.COOKIE, "JSESSIONID=$sessionId")
            }
        }
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        val bodyToken = authProbe.responseBody ?: error("Missing CSRF token from /test/csrf")

        val allCookies = authProbe.responseHeaders[HttpHeaders.SET_COOKIE].orEmpty()
        val responseSessionId = allCookies
            .firstOrNull { it.startsWith("JSESSIONID=") }
            ?.substringAfter("JSESSIONID=")
            ?.substringBefore(';')

        val cookies = buildString {
            val effectiveSessionId = responseSessionId ?: sessionId
            if (effectiveSessionId != null) {
                append("JSESSIONID=$effectiveSessionId; ")
            }
            append("XSRF-TOKEN=$bodyToken")
        }

        return CsrfContext(
            cookies = cookies,
            csrfToken = bodyToken,
        )
    }

    protected fun authenticatedCsrfContext(email: String): CsrfContext {
        val sessionId = restTestClient.get().uri("/test/login")
            .header(TEST_USER_EMAIL_HEADER, email)
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody
            ?: error("Missing session id from /test/login")

        return csrfContext(sessionId)
    }

    protected fun guestSessionCookie(guestId: GuestId): String {
        val token = guestSessionTokens.issue(
            GuestSession(guestId = guestId, invitationId = bridesMaidInvitation.id)
        )

        return "$GUEST_SESSION_COOKIE=$token"
    }
}

