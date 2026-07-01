package me.elgregos.theweddingplan.domain.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import kotlin.test.Test
import kotlin.test.assertFailsWith

class InvitationTest {

    @Test
    fun `should require at least one guest`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Invitation(title = "Empty invitation", guestIds = emptySet())
        }

        assertThat(exception).messageContains("at least one guest")
    }

    @Test
    fun `should create invitation with guest ids`() {
        val invitation = Invitation(title = "Ceremony", guestIds = setOf(johnDoe.id))

        assertThat(invitation.guestIds).isEqualTo(setOf(johnDoe.id))
    }
}
