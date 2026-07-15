package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest.CsrfContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

object GuestApiTestHelper {

    fun createGuest(restTestClient: RestTestClient, csrf: CsrfContext, request: AddGuestRequest): GuestResponse =
        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created guest in response body")
}