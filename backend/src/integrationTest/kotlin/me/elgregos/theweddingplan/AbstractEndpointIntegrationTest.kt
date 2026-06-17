package me.elgregos.theweddingplan

import me.elgregos.theweddingplan.TestAuthenticationConfig.Companion.TEST_USER_EMAIL_HEADER
import org.springframework.http.HttpHeaders
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractEndpointIntegrationTest : AbstractIntegrationTest() {
    protected data class CsrfContext(
        val cookies: String,
        val csrfToken: String,
    )

    @LocalServerPort
    protected var port: Int = 0

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
            .returnResult(String::class.java)

        val allCookies = authProbe.responseHeaders[HttpHeaders.SET_COOKIE].orEmpty()
        val xsrfCookie = allCookies.firstOrNull { it.startsWith("XSRF-TOKEN=") }
            ?: error("Missing XSRF-TOKEN cookie")
        val encodedToken = xsrfCookie.substringAfter("XSRF-TOKEN=").substringBefore(';')
        val xsrfToken = authProbe.responseBody ?: error("Missing CSRF token from /test/csrf")

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
}

