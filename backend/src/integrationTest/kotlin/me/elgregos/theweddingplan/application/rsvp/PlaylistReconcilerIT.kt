package me.elgregos.theweddingplan.application.rsvp

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import me.elgregos.theweddingplan.infrastructure.rsvp.repository.GuestRsvpExposedRepository
import me.elgregos.theweddingplan.support.RecordingWeddingPlaylist
import me.elgregos.theweddingplan.support.RecordingWeddingPlaylistConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.BeforeTest
import kotlin.test.Test

@Import(RecordingWeddingPlaylistConfig::class)
class PlaylistReconcilerIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var playlistReconciler: PlaylistReconciler

    @Autowired
    private lateinit var guestRsvps: GuestRsvpExposedRepository

    @Autowired
    private lateinit var recordingWeddingPlaylist: RecordingWeddingPlaylist

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeTest
    fun cleanUp() {
        jdbcTemplate.execute("truncate table guest_rsvp")
        recordingWeddingPlaylist.reset()
    }

    @Test
    fun `should push a pending song to the playlist`() {
        guestRsvps.save(johnDoeRsvpWithChoices)

        playlistReconciler.reconcile()

        assertThat(recordingWeddingPlaylist.addedTrackIds).containsExactly(laVieEnRose.deezerId)
    }

    @Test
    fun `should flag a reconciled song as synchronized`() {
        guestRsvps.save(johnDoeRsvpWithChoices)

        playlistReconciler.reconcile()

        assertThat(guestRsvps.findGuestsWithUnsynchronizedSong()).isEmpty()
        assertThat(guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should leave a song pending when the playlist sync fails`() {
        recordingWeddingPlaylist.failing = true
        guestRsvps.save(johnDoeRsvpWithChoices)

        playlistReconciler.reconcile()

        assertThat(guestRsvps.isSongOnPlaylist(laVieEnRose.deezerId)).isFalse()
    }
}

