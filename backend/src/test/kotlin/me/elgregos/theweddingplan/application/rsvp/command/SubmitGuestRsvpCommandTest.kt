package me.elgregos.theweddingplan.application.rsvp.command

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommandFixtures.johnDoeAttending
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import kotlin.test.Test

class SubmitGuestRsvpCommandTest {

    @Test
    fun `should convert the command to a new rsvp keeping guest attendance and answers`() {
        val rsvp = johnDoeAttending.toGuestRsvp()

        assertThat(rsvp).all {
            prop(GuestRsvp::guestId).isEqualTo(johnDoeAttending.guestId)
            prop(GuestRsvp::attendance).isEqualTo(johnDoeAttending.attendance)
            prop(GuestRsvp::answers).isEqualTo(johnDoeAttending.answers)
            prop(GuestRsvp::version).isEqualTo(1L)
        }
    }
}


