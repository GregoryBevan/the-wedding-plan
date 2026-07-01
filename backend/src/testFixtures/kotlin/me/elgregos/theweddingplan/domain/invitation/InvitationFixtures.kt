package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import java.time.LocalDate
import java.time.LocalDateTime

object InvitationFixtures {

    val creationDate: LocalDateTime = LocalDateTime.of(2026, 7, 1, 10, 45, 28)

    val ceremonyInvitation = Invitation(
        id = InvitationId.fromString("019f5f7d-258c-7638-9f26-0f0ab53e8dcb"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Ceremony",
        eventDate = LocalDate.of(2027, 6, 12),
        guestIds = setOf(johnDoe.id),
    )

    val familyInvitation = Invitation(
        id = InvitationId.fromString("019f5f72-cbb3-7a52-aebb-5d2fbba19b3a"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Family table",
        eventDate = LocalDate.of(2027, 6, 12),
        guestIds = setOf(johnDoe.id, janeDoe.id),
    )
}

