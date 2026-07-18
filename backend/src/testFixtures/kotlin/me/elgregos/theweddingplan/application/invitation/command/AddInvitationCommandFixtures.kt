package me.elgregos.theweddingplan.application.invitation.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation

object AddInvitationCommandFixtures {
    val brideFamily = AddInvitationCommand(
        label = brideFamilyInvitation.label,
        description = brideFamilyInvitation.description,
        guestIds = setOf(johnDoe.id),
    )

    val mixedGuests = AddInvitationCommand(
        label = "Mixed guests",
        description = "Mixed guests invitation",
        guestIds = setOf(johnDoe.id, janeDoe.id),
    )

    val missingGuests = AddInvitationCommand(
        label = "Missing guests",
        description = "Missing guests invitation",
        guestIds = setOf(
            GuestId.fromString("019f70eb-f060-7d9f-8dd8-f9caeca9d078"),
            GuestId.fromString("019f70ec-4f88-77f9-ad98-d0390f978ee0"),
        ),
    )

    val noGuest = AddInvitationCommand(
        label = "No guests",
        guestIds = emptySet(),
        description = "No guests"
    )
}
