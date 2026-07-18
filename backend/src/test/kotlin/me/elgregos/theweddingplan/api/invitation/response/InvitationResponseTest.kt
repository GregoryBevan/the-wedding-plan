package me.elgregos.theweddingplan.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.brideFamily
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.friends
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.friendsInvitation
import kotlin.test.Test

class InvitationResponseTest {

    @Test
    fun `should map invitation to response`() {
        val response = brideFamilyInvitation.toResponse()

        assertThat(response).isEqualTo(brideFamily)
    }

    @Test
    fun `should sort guests by id when mapping invitation`() {
        val response = friendsInvitation.toResponse()

        assertThat(response).isEqualTo(friends)
    }
}

