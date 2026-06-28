package me.elgregos.theweddingplan.domain.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeDeleted
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeRestored
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeUpdated
import kotlin.test.Test

class GuestTest {

    @Test
    fun `should update guest details`() {
        val updatedGuest = johnDoe.updateDetails(
            firstName = "Johnny",
            lastName = "Doe",
            email = "johnny.doe@example.com",
            now = johnDoe.creationDate.plusDays(1),
        )

        assertThat(updatedGuest).isEqualTo(johnDoeUpdated)
    }

    @Test
    fun `should mark guest as deleted`() {
        val deletedGuest = johnDoe.markAsDeleted(now = johnDoe.creationDate.plusDays(2))

        assertThat(deletedGuest).isEqualTo(johnDoeDeleted)
    }

    @Test
    fun `should keep guest unchanged when already deleted`() {
        val alreadyDeletedGuest = johnDoeDeleted.markAsDeleted(now = johnDoe.creationDate.plusDays(3))

        assertThat(alreadyDeletedGuest).isEqualTo(johnDoeDeleted)
    }

    @Test
    fun `should restore deleted guest`() {
        val restoredGuest = johnDoeDeleted.restore(now = johnDoe.creationDate.plusDays(3))

        assertThat(restoredGuest).isEqualTo(johnDoeRestored)
    }

    @Test
    fun `should keep guest unchanged when already active`() {
        val alreadyActiveGuest = johnDoe.restore(now = johnDoe.creationDate.plusDays(3))

        assertThat(alreadyActiveGuest).isEqualTo(johnDoe)
    }
}
