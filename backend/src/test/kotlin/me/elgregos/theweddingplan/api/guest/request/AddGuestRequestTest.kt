package me.elgregos.theweddingplan.api.guest.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.guest.request.AddGuestRequestFixtures.charlieDavis
import me.elgregos.theweddingplan.application.guest.command.AddGuestCommandFixtures.charlieDavis as charlieDavisCommand
import me.elgregos.theweddingplan.domain.guest.entity.Language
import kotlin.test.Test

class AddGuestRequestTest {

    @Test
    fun `should map request to command`() {
        assertThat(charlieDavis.toCommand(Language.FR)).isEqualTo(charlieDavisCommand)
    }
}

