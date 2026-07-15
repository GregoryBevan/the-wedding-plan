package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.brideFamilyInvitation

object UpdateInvitationCommandFixtures {
    val brideFamily = UpdateInvitationCommand(
        id = brideFamilyInvitation.id,
        version = brideFamilyInvitation.version,
        label = "Bride Family Updated",
        description = "Bride family invitation updated",
        guestIds = setOf(johnDoe.id),
    )

    val mixedGuests = UpdateInvitationCommand(
        id = brideFamilyInvitation.id,
        version = brideFamilyInvitation.version,
        label = "Mixed guests",
        description = "Mixed guests invitation",
        guestIds = setOf(johnDoe.id, janeDoe.id),
    )

    val missingGuests = UpdateInvitationCommand(
        id = brideFamilyInvitation.id,
        version = brideFamilyInvitation.version,
        label = "Missing guests",
        description = "Missing guests invitation",
        guestIds = setOf(
            GuestId.fromString("019f70eb-f060-7d9f-8dd8-f9caeca9d078"),
            GuestId.fromString("019f70ec-4f88-77f9-ad98-d0390f978ee0"),
        ),
    )

    val noGuest = UpdateInvitationCommand(
        id = brideFamilyInvitation.id,
        version = brideFamilyInvitation.version,
        label = "No guests",
        description = "No guests",
        guestIds = emptySet(),
    )
}
