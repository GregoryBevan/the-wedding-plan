package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeArchived
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
    fun `should restore archived guest`() {
        every { guests.findArchivedById(johnDoeArchived.id) } returns johnDoeArchived
        every { guests.restore(any(), johnDoeArchived.version) } returns johnDoeRestored

        val result = guestRestorer.restore(johnDoeArchived.id)

        assertThat(result).isEqualTo(RestoreGuestResult.Restored(johnDoeRestored))
    }

    @Test
    fun `should return not found when guest is not archived`() {
        every { guests.findArchivedById(johnDoeArchived.id) } returns null

        val result = guestRestorer.restore(johnDoeArchived.id)

        assertThat(result).isEqualTo(RestoreGuestResult.NotFound)
    }

    @Test
    fun `should return version conflict when repository detects stale version`() {
        every { guests.findArchivedById(johnDoeArchived.id) } returns johnDoeArchived
        every { guests.restore(any(), johnDoeArchived.version) } returns null

        val result = guestRestorer.restore(johnDoeArchived.id)

        assertThat(result).isEqualTo(RestoreGuestResult.VersionConflict)
    }
}

