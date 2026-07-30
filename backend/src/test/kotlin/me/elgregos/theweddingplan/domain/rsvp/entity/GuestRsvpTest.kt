package me.elgregos.theweddingplan.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpUpdated
import me.elgregos.theweddingplan.domain.shared.Dates
import kotlin.test.Test

class GuestRsvpTest {

    @Test
    fun `should create a first response at version 1`() {
        val rsvp = GuestRsvp(guestId = johnDoe.id, attendance = RsvpAttendance.ATTENDING)

        assertThat(rsvp.version).isEqualTo(1L)
    }

    @Test
    fun `should bump version and update date when responding`() {
        val respondedRsvp = johnDoeRsvp.respond(
            attendance = RsvpAttendance.DECLINED,
            now = johnDoeRsvp.creationDate.plusDays(1),
        )

        assertThat(respondedRsvp).isEqualTo(johnDoeRsvpUpdated)
    }

    @Test
    fun `should default the update date to now when responding`() {
        val before = Dates.nowUtcMillis()

        val respondedRsvp = johnDoeRsvp.respond(RsvpAttendance.ATTENDING)

        assertThat(respondedRsvp.updateDate).isBetween(before, Dates.nowUtcMillis())
    }
}

