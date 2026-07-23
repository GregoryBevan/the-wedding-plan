package me.elgregos.theweddingplan.api.invitation

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.invitation.response.PublicInvitationGuestResponse
import me.elgregos.theweddingplan.api.invitation.response.PublicInvitationResponse
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.malformedToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.unknownToken
import org.springframework.http.MediaType
import kotlin.test.Test

class GuestAccessInvitationEndpointIT : AbstractEndpointIntegrationTest() {

    @Test
    fun `should resolve public invitation for a valid token`() {
        val response = restTestClient.get().uri("/guest-access/invitations/${bridesMaidInvitation.accessToken.value}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(PublicInvitationResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected resolved invitation in response body")

        val expectedGuests = bridesMaidInvitation.guests
            .sortedBy { "${it.id}" }
            .map {
                PublicInvitationGuestResponse(
                    id = "${it.id}",
                    firstName = it.firstName,
                    lastName = it.lastName,
                )
            }

        assertThat(response).all {
            prop(PublicInvitationResponse::label).isEqualTo(bridesMaidInvitation.label)
            prop(PublicInvitationResponse::description).isEqualTo(bridesMaidInvitation.description)
            prop(PublicInvitationResponse::guestCount).isEqualTo(bridesMaidInvitation.guests.size)
            prop(PublicInvitationResponse::guests).isEqualTo(expectedGuests)
        }
    }

    @Test
    fun `should return not found when token does not match an invitation`() {
        restTestClient.get().uri("/guest-access/invitations/$unknownToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return bad request for malformed token`() {
        restTestClient.get().uri("/guest-access/invitations/$malformedToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

}


