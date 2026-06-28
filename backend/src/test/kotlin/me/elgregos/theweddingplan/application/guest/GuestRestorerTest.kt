package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeDeleted
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeRestored
import me.elgregos.theweddingplan.domain.guest.Guests
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestRestorerTest {

    private lateinit var guests: Guests
    private lateinit var guestRestorer: GuestRestorer

    @BeforeTest
    fun setUp() {
        guests = mockk()
        guestRestorer = GuestRestorer(guests)
    }

    @Test
    fun `should restore deleted guest`() {
        every { guests.findDeletedById(johnDoeDeleted.id) } returns johnDoeDeleted
        every { guests.restore(any(), johnDoeDeleted.version) } returns johnDoeRestored

        val result = guestRestorer.restore(johnDoeDeleted.id)

        assertThat(result).isEqualTo(RestoreGuestResult.Restored(johnDoeRestored))
    }

    @Test
    fun `should return not found when guest is not deleted`() {
        every { guests.findDeletedById(johnDoeDeleted.id) } returns null

        val result = guestRestorer.restore(johnDoeDeleted.id)

        assertThat(result).isEqualTo(RestoreGuestResult.NotFound)
    }

    @Test
    fun `should return version conflict when repository detects stale version`() {
        every { guests.findDeletedById(johnDoeDeleted.id) } returns johnDoeDeleted
        every { guests.restore(any(), johnDoeDeleted.version) } returns null

        val result = guestRestorer.restore(johnDoeDeleted.id)

        assertThat(result).isEqualTo(RestoreGuestResult.VersionConflict)
    }
}

