package me.elgregos.theweddingplan.domain.invitation

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.ceremonyInvitation
import kotlin.test.Test

class InvitationTest {

    @Test
    fun `should create invitation with guest ids`() {
        assertThat(ceremonyInvitation.guestIds).isEqualTo(setOf(johnDoe.id))
    }

    @Test
    fun `should require at least one guest`() {
        assertFailure {     Invitation(label = "Empty invitation", guestIds = emptySet()) }
            .isInstanceOf(IllegalArgumentException::class)
            .messageContains("at least one guest")
    }
}
