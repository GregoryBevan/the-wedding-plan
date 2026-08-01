package me.elgregos.theweddingplan.application.rsvp.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance

data class SubmitGuestRsvpCommand(
    val guestId: GuestId,
    val attendance: RsvpAttendance,
)

