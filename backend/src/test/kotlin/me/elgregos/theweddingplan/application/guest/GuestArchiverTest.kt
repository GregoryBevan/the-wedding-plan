package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeDeleted
import me.elgregos.theweddingplan.domain.guest.Guests
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestArchiverTest {

    private lateinit var guests: Guests
    private lateinit var guestArchiver: GuestArchiver

    @BeforeTest
    fun setUp() {
        guests = mockk()
        guestArchiver = GuestArchiver(guests)
    }

    @Test
    fun `should archive existing guest`() {
        every { guests.findById(johnDoe.id) } returns johnDoe
        every { guests.update(any(), johnDoe.version) } returns johnDoeDeleted

        val result = guestArchiver.archive(johnDoe.id)

        assertThat(result).isEqualTo(ArchiveGuestResult.Archived(johnDoeDeleted))
    }

    @Test
    fun `should return not found when archiving non existing guest`() {
        every { guests.findById(johnDoe.id) } returns null

        val result = guestArchiver.archive(johnDoe.id)

        assertThat(result).isEqualTo(ArchiveGuestResult.NotFound)
    }

    @Test
    fun `should return version conflict when repository detects stale version`() {
        every { guests.findById(johnDoe.id) } returns johnDoe
        every { guests.update(any(), johnDoe.version) } returns null

        val result = guestArchiver.archive(johnDoe.id)

        assertThat(result).isEqualTo(ArchiveGuestResult.VersionConflict)
    }
}

