package me.elgregos.theweddingplan.application.rsvp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRoseSynced
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import me.elgregos.theweddingplan.domain.song.WeddingPlaylist
import me.elgregos.theweddingplan.domain.song.WeddingPlaylistUnavailableException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlaylistSynchronizerTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var weddingPlaylist: WeddingPlaylist
    private lateinit var playlistSynchronizer: PlaylistSynchronizer

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk(relaxed = true)
        weddingPlaylist = mockk(relaxed = true)
        playlistSynchronizer = PlaylistSynchronizer(guestRsvps, weddingPlaylist)
    }

    @Test
    fun `should add a song that is not yet on the playlist`() {
        every { guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId) } returns false

        playlistSynchronizer.sync(janeDoe.id, laVieEnRose)

        verify(exactly = 1) { weddingPlaylist.addTrack(laVieEnRose.deezerId) }
    }

    @Test
    fun `should flag the song as synchronized once added`() {
        every { guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId) } returns false

        playlistSynchronizer.sync(janeDoe.id, laVieEnRose)

        verify(exactly = 1) { guestRsvps.markSongSynchronized(janeDoe.id) }
    }

    @Test
    fun `should flag as synchronized without adding a song already on the playlist`() {
        every { guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId) } returns true

        playlistSynchronizer.sync(janeDoe.id, laVieEnRose)

        verify(exactly = 0) { weddingPlaylist.addTrack(any()) }
        verify(exactly = 1) { guestRsvps.markSongSynchronized(janeDoe.id) }
    }

    @Test
    fun `should do nothing for an already synchronized song`() {
        playlistSynchronizer.sync(janeDoe.id, laVieEnRoseSynced)

        verify(exactly = 0) { guestRsvps.isSongOnPlaylist(any()) }
        verify(exactly = 0) { weddingPlaylist.addTrack(any()) }
    }

    @Test
    fun `should leave the song pending when adding it fails`() {
        every { guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId) } returns false
        every { weddingPlaylist.addTrack(any()) } throws WeddingPlaylistUnavailableException("boom")

        playlistSynchronizer.sync(janeDoe.id, laVieEnRose)

        verify(exactly = 0) { guestRsvps.markSongSynchronized(any()) }
    }

    @Test
    fun `should add a song only once when synced concurrently for the same track`() {
        val synced = AtomicBoolean(false)
        every { guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId) } answers { synced.get() }
        every { weddingPlaylist.addTrack(laVieEnRose.deezerId) } answers { Thread.sleep(20) }
        every { guestRsvps.markSongSynchronized(janeDoe.id) } answers { synced.set(true) }

        val ready = CountDownLatch(1)
        val threads = (1..8).map {
            thread {
                ready.await()
                playlistSynchronizer.sync(janeDoe.id, laVieEnRose)
            }
        }
        ready.countDown()
        threads.forEach { it.join() }

        verify(exactly = 1) { weddingPlaylist.addTrack(laVieEnRose.deezerId) }
    }

    @Test
    fun `should remove a track no guest chooses anymore`() {
        every { guestRsvps.isSongChosenByAnyGuest(laVieEnRose.deezerId) } returns false

        playlistSynchronizer.unsync(laVieEnRose.deezerId)

        verify(exactly = 1) { weddingPlaylist.removeTrack(laVieEnRose.deezerId) }
    }

    @Test
    fun `should keep a track another guest still chooses`() {
        every { guestRsvps.isSongChosenByAnyGuest(laVieEnRose.deezerId) } returns true

        playlistSynchronizer.unsync(laVieEnRose.deezerId)

        verify(exactly = 0) { weddingPlaylist.removeTrack(any()) }
    }

    @Test
    fun `should swallow a failure when removing a track`() {
        every { guestRsvps.isSongChosenByAnyGuest(laVieEnRose.deezerId) } returns false
        every { weddingPlaylist.removeTrack(any()) } throws WeddingPlaylistUnavailableException("boom")

        playlistSynchronizer.unsync(laVieEnRose.deezerId)

        verify(exactly = 1) { weddingPlaylist.removeTrack(laVieEnRose.deezerId) }
    }
}



