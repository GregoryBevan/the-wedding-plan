package me.elgregos.theweddingplan.api.rsvp.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.attending
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.attendingUnknownMeal
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.attendingVeggie
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.attendingWithoutMeal
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.declined
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequestFixtures.invalidAttendance
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttending
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttendingVeggie
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeDeclined
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import kotlin.test.Test

class SubmitRsvpRequestTest {

    @Test
    fun `should map an attending request with meal and song to a command`() {
        assertThat(attending.toCommand(johnDoe.id)).isEqualTo(johnDoeAttending)
    }

    @Test
    fun `should map an attending request with meal only to a command`() {
        assertThat(attendingVeggie.toCommand(johnDoe.id)).isEqualTo(johnDoeAttendingVeggie)
    }

    @Test
    fun `should map a declined request ignoring choices`() {
        assertThat(declined.toCommand(johnDoe.id)).isEqualTo(johnDoeDeclined)
    }

    @Test
    fun `should map to null when attendance is invalid`() {
        assertThat(invalidAttendance.toCommand(johnDoe.id)).isNull()
    }

    @Test
    fun `should map to null when attending without a meal`() {
        assertThat(attendingWithoutMeal.toCommand(johnDoe.id)).isNull()
    }

    @Test
    fun `should map to null when attending with an unknown meal`() {
        assertThat(attendingUnknownMeal.toCommand(johnDoe.id)).isNull()
    }
}

