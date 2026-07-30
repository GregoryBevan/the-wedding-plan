package me.elgregos.theweddingplan.domain.rsvp.entity

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import java.time.LocalDateTime

object GuestRsvpFixtures {
    val creationDate: LocalDateTime = LocalDateTime.of(2026, 6, 13, 10, 0, 0)

    val johnDoeRsvp = GuestRsvp(
        id = GuestRsvpId.fromString("019fb445-4209-75ad-9370-e16ff6140b37"),
        guestId = johnDoe.id,
        version = 1L,
        creationDate = creationDate,
        updateDate = creationDate,
        attendance = RsvpAttendance.ATTENDING,
    )

    val johnDoeRsvpUpdated = johnDoeRsvp.copy(
        version = 2L,
        updateDate = creationDate.plusDays(1),
        attendance = RsvpAttendance.DECLINED,
    )
}


