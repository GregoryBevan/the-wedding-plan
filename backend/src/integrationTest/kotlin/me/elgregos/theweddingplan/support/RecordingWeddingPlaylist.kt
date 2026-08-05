package me.elgregos.theweddingplan.support

import me.elgregos.theweddingplan.domain.song.WeddingPlaylist
import me.elgregos.theweddingplan.domain.song.WeddingPlaylistUnavailableException
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class RecordingWeddingPlaylistConfig {

    @Bean
    @Primary
    fun recordingWeddingPlaylist() = RecordingWeddingPlaylist()
}

/**
 * Test double for [WeddingPlaylist] that records the tracks it is asked to add or remove (and can
 * simulate a provider failure), so the playlist-sync flow is exercised without hitting the real
 * Deezer API.
 */
class RecordingWeddingPlaylist : WeddingPlaylist {
    val addedTrackIds = mutableListOf<Long>()
    val removedTrackIds = mutableListOf<Long>()
    var failing = false

    override fun addTrack(deezerId: Long) {
        if (failing) throw WeddingPlaylistUnavailableException("upstream down")
        addedTrackIds += deezerId
    }

    override fun removeTrack(deezerId: Long) {
        if (failing) throw WeddingPlaylistUnavailableException("upstream down")
        removedTrackIds += deezerId
    }

    fun reset() {
        addedTrackIds.clear()
        removedTrackIds.clear()
        failing = false
    }
}

