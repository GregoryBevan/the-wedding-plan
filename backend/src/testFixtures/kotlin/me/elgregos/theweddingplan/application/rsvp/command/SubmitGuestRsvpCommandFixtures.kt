package me.elgregos.theweddingplan.application.rsvp.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeAnswers
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.veggieAnswers
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance

object SubmitGuestRsvpCommandFixtures {

    val johnDoeAttending = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.ATTENDING,
        answers = johnDoeAnswers,
    )

    val johnDoeAttendingVeggie = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.ATTENDING,
        answers = veggieAnswers,
    )

    val johnDoeDeclined = SubmitGuestRsvpCommand(
        guestId = johnDoe.id,
        attendance = RsvpAttendance.DECLINED,
    )
}

