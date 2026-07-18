package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest.CsrfContext
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequest
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

object InvitationApiTestHelper {

    fun createInvitation(restTestClient: RestTestClient, csrf: CsrfContext, request: AddInvitationRequest): InvitationResponse =
        restTestClient.post().uri("/api/invitations")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(InvitationResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created invitation in response body")
}