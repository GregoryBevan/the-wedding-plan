package me.elgregos.theweddingplan.application.rsvp

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.janeDoeRsvpWithSameSyncedChoicesAsJohnDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithSyncedChoices
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.veggieAnswers
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import me.elgregos.theweddingplan.infrastructure.rsvp.repository.GuestRsvpExposedRepository
import me.elgregos.theweddingplan.support.RecordingWeddingPlaylist
import me.elgregos.theweddingplan.support.RecordingWeddingPlaylistConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.BeforeTest
import kotlin.test.Test


@Import(RecordingWeddingPlaylistConfig::class)
class PlaylistSynchronizerIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var playlistSynchronizer: PlaylistSynchronizer

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
    fun `should keep a song another guest still chose when one guest drops it`() {
        guestRsvps.save(johnDoeRsvpWithSyncedChoices)
        guestRsvps.save(janeDoeRsvpWithSameSyncedChoicesAsJohnDoe)
        guestRsvps.save(janeDoeRsvpWithSameSyncedChoicesAsJohnDoe.respond(RsvpAttendance.ATTENDING, veggieAnswers))

        playlistSynchronizer.unsync(laVieEnRose.deezerId)

        assertThat(recordingWeddingPlaylist.removedTrackIds).isEmpty()
    }

    @Test
    fun `should remove a song once the last guest drops it`() {
        guestRsvps.save(johnDoeRsvpWithSyncedChoices)
        guestRsvps.save(johnDoeRsvpWithSyncedChoices.respond(RsvpAttendance.ATTENDING, veggieAnswers))

        playlistSynchronizer.unsync(laVieEnRose.deezerId)

        assertThat(recordingWeddingPlaylist.removedTrackIds).containsExactly(laVieEnRose.deezerId)
    }
}

