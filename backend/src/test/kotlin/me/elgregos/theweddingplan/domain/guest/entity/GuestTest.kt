package me.elgregos.theweddingplan.domain.guest.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoeArchived
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoeRestored
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoeUpdated
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
    fun `should mark guest as archived`() {
        val archivedGuest = johnDoe.markAsArchived(now = johnDoe.creationDate.plusDays(2))

        assertThat(archivedGuest).isEqualTo(johnDoeArchived)
    }

    @Test
    fun `should keep guest unchanged when already archived`() {
        val alreadyArchivedGuest = johnDoeArchived.markAsArchived(now = johnDoe.creationDate.plusDays(3))

        assertThat(alreadyArchivedGuest).isEqualTo(johnDoeArchived)
    }

    @Test
    fun `should restore archived guest`() {
        val restoredGuest = johnDoeArchived.restore(now = johnDoe.creationDate.plusDays(3))

        assertThat(restoredGuest).isEqualTo(johnDoeRestored)
    }

    @Test
    fun `should keep guest unchanged when already active`() {
        val alreadyActiveGuest = johnDoe.restore(now = johnDoe.creationDate.plusDays(3))

        assertThat(alreadyActiveGuest).isEqualTo(johnDoe)
    }
}
