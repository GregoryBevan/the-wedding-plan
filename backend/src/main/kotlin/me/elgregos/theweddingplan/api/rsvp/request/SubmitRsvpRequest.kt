package me.elgregos.theweddingplan.api.rsvp.request

import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.Meal
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAnswers
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance

data class SubmitRsvpRequest(
    val attendance: String,
    val meal: String? = null,
    val song: SubmitSongRequest? = null,
) {
    /**
     * Builds the command, or returns `null` (→ 400) for an invalid payload:
     * an unknown attendance, or — when attending — a missing/unknown meal.
     * Choices are ignored for a declined guest.
     */
    internal fun toCommand(guestId: GuestId): SubmitGuestRsvpCommand? {
        val attendance = RsvpAttendance.parseOrNull(attendance) ?: return null

        if (attendance != RsvpAttendance.ATTENDING) {
            return SubmitGuestRsvpCommand(guestId = guestId, attendance = attendance)
        }

        val meal = Meal.parseOrNull(meal) ?: return null
        return SubmitGuestRsvpCommand(
            guestId = guestId,
            attendance = attendance,
            answers = RsvpAnswers(meal = meal, song = song?.toSongChoice()),
        )
    }
}

