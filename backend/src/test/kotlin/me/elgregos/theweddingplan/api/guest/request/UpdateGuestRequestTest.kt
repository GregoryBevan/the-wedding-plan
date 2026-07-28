package me.elgregos.theweddingplan.api.guest.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.guest.request.UpdateGuestRequestFixtures.johnDoeUpdated
import me.elgregos.theweddingplan.application.guest.command.UpdateGuestCommandFixtures.johnDoeUpdated as johnDoeUpdatedCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.Language
import kotlin.test.Test

class UpdateGuestRequestTest {

    @Test
    fun `should map request to command`() {
        val command = johnDoeUpdated.toCommand(johnDoe.id, Language.FR)

        assertThat(command).isEqualTo(johnDoeUpdatedCommand)
    }
}

