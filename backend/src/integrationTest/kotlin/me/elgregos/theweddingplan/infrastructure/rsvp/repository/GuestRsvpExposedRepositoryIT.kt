package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpUpdated
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
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

        val mismatched = johnDoeRsvpUpdated.copy(
            id = GuestRsvpId(),
            creationDate = johnDoeRsvp.creationDate.plusDays(5),
        )
        val saved = guestRsvpsRepository.save(mismatched)

        assertThat(saved).isEqualTo(johnDoeRsvpUpdated)
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



