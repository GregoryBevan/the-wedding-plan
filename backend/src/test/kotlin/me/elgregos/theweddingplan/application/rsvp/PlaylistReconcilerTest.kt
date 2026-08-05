package me.elgregos.theweddingplan.application.rsvp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import me.elgregos.theweddingplan.domain.rsvp.repository.PendingSongSync
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlaylistReconcilerTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var playlistSynchronizer: PlaylistSynchronizer
    private lateinit var playlistReconciler: PlaylistReconciler

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        playlistSynchronizer = mockk(relaxed = true)
        playlistReconciler = PlaylistReconciler(guestRsvps, playlistSynchronizer)
    }

    @Test
    fun `should re-drive every pending song sync`() {
        every { guestRsvps.findGuestsWithUnsynchronizedSong() } returns listOf(
            PendingSongSync(johnDoe.id, laVieEnRose),
            PendingSongSync(janeDoe.id, laVieEnRose),
        )

        playlistReconciler.reconcile()

        verify(exactly = 1) { playlistSynchronizer.sync(johnDoe.id, laVieEnRose) }
        verify(exactly = 1) { playlistSynchronizer.sync(janeDoe.id, laVieEnRose) }
    }

    @Test
    fun `should do nothing when there is no pending song`() {
        every { guestRsvps.findGuestsWithUnsynchronizedSong() } returns emptyList()

        playlistReconciler.reconcile()

        verify(exactly = 0) { playlistSynchronizer.sync(any(), any()) }
    }
}

