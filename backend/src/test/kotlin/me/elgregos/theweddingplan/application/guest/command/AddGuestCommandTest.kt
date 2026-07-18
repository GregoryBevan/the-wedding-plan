package me.elgregos.theweddingplan.application.guest.command

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import me.elgregos.theweddingplan.application.guest.command.AddGuestCommandFixtures.charlieDavis
import me.elgregos.theweddingplan.domain.guest.entity.Guest
import kotlin.test.Test

class AddGuestCommandTest {

    @Test
    fun `should convert command to guest`() {
        assertThat(charlieDavis.toGuest()).all {
            prop(Guest::id).isNotNull()
            prop(Guest::version).isEqualTo(1L)
            prop(Guest::creationDate).isNotNull()
            prop(Guest::updateDate).isNotNull()
            prop(Guest::firstName).isEqualTo(charlieDavis.firstName)
            prop(Guest::lastName).isEqualTo(charlieDavis.lastName)
            prop(Guest::email).isEqualTo(charlieDavis.email)
        }
    }
}

