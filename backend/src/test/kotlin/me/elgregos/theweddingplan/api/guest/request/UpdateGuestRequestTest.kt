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
    fun `should preserve language when omitted by mapping it to null`() {
        val command = johnDoeUpdated.toCommand(johnDoe.id)

        assertThat(command).isEqualTo(johnDoeUpdatedCommand)
    }

    @Test
    fun `should map a provided language to the command`() {
        val command = johnDoeUpdated.copy(language = "EN").toCommand(johnDoe.id)

        assertThat(command.language).isEqualTo(Language.EN)
    }
}