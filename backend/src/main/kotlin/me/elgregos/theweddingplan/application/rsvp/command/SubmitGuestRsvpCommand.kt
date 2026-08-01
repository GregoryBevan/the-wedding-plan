package me.elgregos.theweddingplan.application.rsvp.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAnswers
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance

data class SubmitGuestRsvpCommand(
    val guestId: GuestId,
    val attendance: RsvpAttendance,
    val answers: RsvpAnswers? = null,
)

internal fun SubmitGuestRsvpCommand.toGuestRsvp() = GuestRsvp(
    guestId = guestId,
    attendance = attendance,
    answers = answers,
)

