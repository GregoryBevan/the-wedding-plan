package me.elgregos.theweddingplan.api.rsvp.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import kotlin.test.Test

class SubmitRsvpRequestTest {

    @Test
    fun `should map request to command for the given guest`() {
        assertThat(SubmitRsvpRequest(attendance = "ATTENDING").toCommand(janeDoe.id))
            .isEqualTo(SubmitGuestRsvpCommand(guestId = janeDoe.id, attendance = RsvpAttendance.ATTENDING))
    }

    @Test
    fun `should map to null when attendance is invalid`() {
        assertThat(SubmitRsvpRequest(attendance = "MAYBE").toCommand(janeDoe.id)).isNull()
    }
}

