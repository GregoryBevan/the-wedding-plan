package me.elgregos.theweddingplan.domain.invitation

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bridesMaidInvitation
import kotlin.test.Test

class InvitationTest {

    @Test
    fun `should create invitation with guests`() {
        assertThat(bridesMaidInvitation.guests).isEqualTo(setOf(janeDoe))
    }

    @Test
    fun `should require at least one guest`() {
        assertFailure { Invitation(label = "Empty invitation", guests = emptySet(), description = "Empty invitation") }
            .isInstanceOf(IllegalArgumentException::class)
            .messageContains("at least one guest")
    }
}
