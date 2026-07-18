package me.elgregos.theweddingplan.api.invitation.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.brideFamilyPublic
import me.elgregos.theweddingplan.api.invitation.response.InvitationResponseFixtures.friendsPublic
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.friendsInvitation
import kotlin.test.Test

class PublicInvitationResponseTest {

    @Test
    fun `should map invitation to public response`() {
        val response = brideFamilyInvitation.toPublicResponse()

        assertThat(response).isEqualTo(brideFamilyPublic)
    }

    @Test
    fun `should sort guests by id when mapping public response`() {
        val response = friendsInvitation.toPublicResponse()

        assertThat(response).isEqualTo(friendsPublic)
    }
}

