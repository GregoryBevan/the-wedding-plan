package me.elgregos.theweddingplan.domain.guest

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.guest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeUpdated
import kotlin.test.Test

class GuestTest {

    @Test
    fun `should create guest`() {
        assertThat(guest()).all {
            prop(Guest::id).isNotNull()
            prop(Guest::version).isEqualTo(1L)
            prop(Guest::creationDate).isNotNull()
            prop(Guest::updateDate).isNotNull()
            prop(Guest::firstName).isEqualTo("John")
            prop(Guest::lastName).isEqualTo("Doe")
            prop(Guest::email).isEqualTo("john.doe@example.com")
        }
    }

    @Test
    fun `should update guest details`() {
        val updatedGuest = johnDoe.updateDetails(
            firstName = "Johnny",
            lastName = "Doe-Smith",
            email = "johnny.doe@example.com",
            now = johnDoe.creationDate.plusDays(1),
        )

        assertThat(updatedGuest).isEqualTo(johnDoeUpdated)
    }
}
