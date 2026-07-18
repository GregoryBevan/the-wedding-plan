package me.elgregos.theweddingplan.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.blankLabel
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.malformedGuestId
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.mixedGuestsWithWhitespace
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.noGuest
import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommandFixtures.mixedGuests
import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommandFixtures.noGuest as noGuestCommand
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import kotlin.test.Test

class UpdateInvitationRequestTest {

    @Test
    fun `should map request to command`() {
        val command = mixedGuestsWithWhitespace.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isEqualTo(mixedGuests)
    }

    @Test
    fun `should return null when label is blank`() {
        val command = blankLabel.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isNull()
    }

    @Test
    fun `should return null when a guest id is malformed`() {
        val command = malformedGuestId.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isNull()
    }

    @Test
    fun `should map request with no guest ids to empty set`() {
        val command = noGuest.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isEqualTo(noGuestCommand)
    }
}

