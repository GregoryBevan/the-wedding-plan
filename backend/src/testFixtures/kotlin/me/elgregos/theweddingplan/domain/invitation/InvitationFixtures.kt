package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.guest.GuestFixtures.emmaWilson
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.liamMiller
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.mickaelKael
import java.time.LocalDateTime

object InvitationFixtures {

    val creationDate: LocalDateTime = LocalDateTime.of(2026, 7, 1, 10, 45, 28)

    val bridesMaidInvitation = Invitation(
        id = InvitationId.fromString("019f2282-6db9-72a3-8d9d-2f6dc80cb89d"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bridesmaid",
        guestIds = setOf(janeDoe.id),
    )

    val bestManInvitation = Invitation(
        id = InvitationId.fromString("019f2282-71a1-7276-87f9-f80375e2570e"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bestman",
        guestIds = setOf(mickaelKael.id),
    )

    val brideFamilyInvitation = Invitation(
        id = InvitationId.fromString("019f2282-7971-77e6-8d25-7568739fca0f"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bride Family",
        guestIds = setOf(johnDoe.id),
    )

    val friendsInvitation = Invitation(
        id = InvitationId.fromString("019f2284-4e31-7a24-be81-1e3affe8b95f"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Friends",
        guestIds = setOf(emmaWilson.id, liamMiller.id),
    )
}
