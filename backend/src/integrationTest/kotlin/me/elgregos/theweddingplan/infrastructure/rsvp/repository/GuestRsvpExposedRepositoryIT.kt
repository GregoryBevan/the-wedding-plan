package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpDeclined
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpMealOnly
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithSyncedChoices
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import me.elgregos.theweddingplan.domain.rsvp.repository.PendingSongSync
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class GuestRsvpExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var guestRsvpsRepository: GuestRsvpExposedRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeTest
    fun cleanRsvps() {
        jdbcTemplate.execute("truncate table guest_rsvp")
    }

    @Test
    fun `should insert a new rsvp`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(rsvpById(johnDoeRsvp.id)).isEqualTo(johnDoeRsvp)
    }

    @Test
    fun `should upsert without creating a duplicate row`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        val updated = johnDoeRsvp.respond(RsvpAttendance.DECLINED, now = johnDoeRsvp.creationDate.plusDays(1))
        guestRsvpsRepository.save(updated)

        assertThat(rsvpCount()).isEqualTo(1)
        assertThat(rsvpById(johnDoeRsvp.id)).isEqualTo(updated)
    }

    @Test
    fun `should return the stored row when upserting ignoring a mismatched surrogate id`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        val mismatched = johnDoeRsvpDeclined.copy(
            id = GuestRsvpId(),
            creationDate = johnDoeRsvp.creationDate.plusDays(5),
        )
        val saved = guestRsvpsRepository.save(mismatched)

        assertThat(saved).isEqualTo(johnDoeRsvpDeclined)
    }

    @Test
    fun `should not create duplicate rows under concurrent submits for the same guest`() {
        val errors = ConcurrentLinkedQueue<Throwable>()
        val startGate = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)

        repeat(20) {
            executor.submit {
                startGate.await()
                runCatching {
                    guestRsvpsRepository.save(johnDoeRsvp.copy(id = GuestRsvpId()))
                }.onFailure(errors::add)
            }
        }

        startGate.countDown()
        executor.shutdown()
        val terminated = executor.awaitTermination(5, TimeUnit.SECONDS)

        assertThat(terminated).isTrue()
        assertThat(errors).isEmpty()
        assertThat(rsvpCount()).isEqualTo(1)
    }

    @Test
    fun `should find rsvp by guest id`() {
        guestRsvpsRepository.save(johnDoeRsvp)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvp.guestId)).isEqualTo(johnDoeRsvp)
    }

    @Test
    fun `should return null when guest has no rsvp`() {
        assertThat(guestRsvpsRepository.findByGuestId(janeDoe.id)).isNull()
    }

    @Test
    fun `should round-trip answers through the jsonb column`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithChoices.guestId)).isEqualTo(johnDoeRsvpWithChoices)
    }

    @Test
    fun `should not report an unsynced song as on the playlist`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isFalse()
    }

    @Test
    fun `should report a synced song as on the playlist`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should report a chosen song as chosen by a guest even when not synced`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.isSongChosenByAnyGuest(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should not report a song no guest chose as chosen`() {
        guestRsvpsRepository.save(johnDoeRsvpMealOnly)

        assertThat(guestRsvpsRepository.isSongChosenByAnyGuest(laVieEnRose.deezerId)).isFalse()
    }

    @Test
    fun `should list a guest whose song is not synced yet`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        assertThat(guestRsvpsRepository.findGuestsWithUnsynchronizedSong())
            .containsExactly(PendingSongSync(johnDoeRsvpWithChoices.guestId, laVieEnRose))
    }

    @Test
    fun `should not list a guest whose song is already synced`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        assertThat(guestRsvpsRepository.findGuestsWithUnsynchronizedSong()).isEmpty()
    }

    @Test
    fun `should flag a guest's song as synchronized`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(guestRsvpsRepository.isSongOnPlaylist(laVieEnRose.deezerId)).isTrue()
    }

    @Test
    fun `should flip only the sync flag and preserve the other answers`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithChoices.guestId))
            .isEqualTo(johnDoeRsvpWithSyncedChoices)
    }

    @Test
    fun `should keep the guest-facing version when flagging a song as synchronized`() {
        guestRsvpsRepository.save(johnDoeRsvpWithChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithChoices.guestId)

        assertThat(rsvpById(johnDoeRsvpWithChoices.id).version).isEqualTo(johnDoeRsvpWithChoices.version)
    }

    @Test
    fun `should do nothing when flagging a song synchronized for a guest without rsvp`() {
        guestRsvpsRepository.markSongSynchronized(janeDoe.id)

        assertThat(rsvpCount()).isEqualTo(0)
    }

    @Test
    fun `should do nothing when flagging a song synchronized for a guest without a song`() {
        guestRsvpsRepository.save(johnDoeRsvpMealOnly)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpMealOnly.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpMealOnly.guestId)).isEqualTo(johnDoeRsvpMealOnly)
    }

    @Test
    fun `should leave an already synchronized song untouched`() {
        guestRsvpsRepository.save(johnDoeRsvpWithSyncedChoices)

        guestRsvpsRepository.markSongSynchronized(johnDoeRsvpWithSyncedChoices.guestId)

        assertThat(guestRsvpsRepository.findByGuestId(johnDoeRsvpWithSyncedChoices.guestId)).isEqualTo(johnDoeRsvpWithSyncedChoices)
    }

    private fun rsvpCount() =
        jdbcTemplate.queryForObject("select count(*) from guest_rsvp", Int::class.java) ?: 0

    private fun rsvpById(id: GuestRsvpId): GuestRsvp =
        jdbcTemplate.queryForObject(
            """
            select id, guest_id, version, creation_date, update_date, attendance
            from guest_rsvp
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                GuestRsvp(
                    id = GuestRsvpId.fromString(rs.getObject("id", UUID::class.java).toString()),
                    guestId = GuestId.fromString(rs.getObject("guest_id", UUID::class.java).toString()),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    attendance = RsvpAttendance.valueOf(rs.getString("attendance")),
                )
            },
            id.value.toJavaUuid()
        )
}



