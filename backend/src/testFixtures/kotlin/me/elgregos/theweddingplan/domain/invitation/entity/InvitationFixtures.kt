package me.elgregos.theweddingplan.domain.invitation.entity

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures
import java.time.LocalDateTime

object InvitationFixtures {

    val creationDate: LocalDateTime = LocalDateTime.of(2026, 7, 1, 10, 45, 28)

    const val unknownToken = "b7fb2f39-fa8f-4b8d-b47f-f65bc5fd0ad1"
    const val malformedToken = "not-a-uuid"

    val bridesMaidInvitation = Invitation(
        id = InvitationId.fromString("019f2282-6db9-72a3-8d9d-2f6dc80cb89d"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bridesmaid",
        description = "Bridesmaid invitation",
        guests = setOf(GuestFixtures.janeDoe),
        accessToken = InvitationAccessToken("dca71c6f-4b29-43a0-80df-426786ca9075"),
    )

    val bestManInvitation = Invitation(
        id = InvitationId.fromString("019f2282-71a1-7276-87f9-f80375e2570e"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bestman",
        description = "Best man invitation",
        guests = setOf(GuestFixtures.mickaelKael),
        accessToken = InvitationAccessToken("35cae9dd-4e1a-4a01-95c0-4c286cafc3e4"),
    )

    val brideFamilyInvitation = Invitation(
        id = InvitationId.fromString("019f2282-7971-77e6-8d25-7568739fca0f"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Bride Family",
        description = "Bride family invitation",
        guests = setOf(GuestFixtures.johnDoe),
    )

    val friendsInvitation = Invitation(
        id = InvitationId.fromString("019f2284-4e31-7a24-be81-1e3affe8b95f"),
        creationDate = creationDate,
        updateDate = creationDate,
        label = "Friends",
        description = "Friends brunch invitation",
        guests = setOf(GuestFixtures.emmaWilson, GuestFixtures.liamMiller),
    )

    val scienceConferenceInvitation = Invitation(
        label = "Science Conference",
        description = "Welcome to the conference",
        guests = setOf(GuestFixtures.albertEinstein, GuestFixtures.marieCurie) // guest can be replaced in tests if needed
    )

    val scienceConferenceInvitationUpdated = scienceConferenceInvitation.copy(
        label = "Science Conference Updated",
        description = "Welcome to the updated conference",
        guests = setOf(GuestFixtures.marieCurie),
    )
    val nonExistingInvitationId = InvitationId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba32")

    fun nonExistingInvitation(guest: Guest, updateDate: LocalDateTime = creationDate) = Invitation(
        id = nonExistingInvitationId,
        version = 2L,
        updateDate = updateDate,
        label = "Non existing",
        description = "This does not exist",
        guests = setOf(guest)
    )

}