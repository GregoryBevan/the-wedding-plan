package me.elgregos.theweddingplan.api.rsvp.request

import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance

data class SubmitRsvpRequest(
    val attendance: String,
) {
    internal fun toCommand(guestId: GuestId): SubmitGuestRsvpCommand? =
        RsvpAttendance.parseOrNull(attendance)
            ?.let { SubmitGuestRsvpCommand(guestId = guestId, attendance = it) }
}

