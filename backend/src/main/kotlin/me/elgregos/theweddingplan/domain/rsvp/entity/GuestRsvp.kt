package me.elgregos.theweddingplan.domain.rsvp.entity

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.shared.Dates
import java.time.LocalDateTime

data class GuestRsvp(
    val id: GuestRsvpId = GuestRsvpId(),
    val guestId: GuestId,
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val attendance: RsvpAttendance,
) {
    fun respond(
        attendance: RsvpAttendance,
        now: LocalDateTime = Dates.nowUtcMillis(),
    ) = copy(
        version = version + 1,
        updateDate = now,
        attendance = attendance,
    )
}

