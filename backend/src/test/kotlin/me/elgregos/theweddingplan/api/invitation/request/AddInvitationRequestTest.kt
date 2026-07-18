package me.elgregos.theweddingplan.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.blankLabel
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.malformedGuestId
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.mixedGuestsWithWhitespace
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.noGuest
import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommandFixtures.mixedGuests
import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommandFixtures.noGuest as noGuestCommand
import kotlin.test.Test

class AddInvitationRequestTest {

    @Test
    fun `should map request to command`() {
        val command = mixedGuestsWithWhitespace.toCommandOrNull()

        assertThat(command).isEqualTo(mixedGuests)
    }

    @Test
    fun `should return null when label is blank`() {
        val command = blankLabel.toCommandOrNull()

        assertThat(command).isNull()
    }

    @Test
    fun `should return null when a guest id is malformed`() {
        val command = malformedGuestId.toCommandOrNull()

        assertThat(command).isNull()
    }

    @Test
    fun `should map request with no guest ids to empty set`() {
        val command = noGuest.toCommandOrNull()

        assertThat(command).isEqualTo(noGuestCommand)
    }
}
