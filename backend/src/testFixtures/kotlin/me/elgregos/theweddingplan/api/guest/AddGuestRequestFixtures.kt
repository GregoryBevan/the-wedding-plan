package me.elgregos.theweddingplan.api.guest

import java.util.UUID

object AddGuestRequestFixtures {
    val charlieDavis = AddGuestRequest(
        firstName = "Charlie",
        lastName = "Davis",
        email = "charlie.davis@example.com"
    )

    val aliceSmith = AddGuestRequest(
        firstName = "Alice",
        lastName = "Smith",
        email = "alice.smith@example.com"
    )

    val bobJohnson = AddGuestRequest(
        firstName = "Bob",
        lastName = "Johnson",
        email = "bob.johnson@example.com"
    )

    val activeGuest = AddGuestRequest(
        firstName = "Active",
        lastName = "Guest",
        email = "active.guest@example.com"
    )

    val archivedGuest = AddGuestRequest(
        firstName = "Archived",
        lastName = "Guest",
        email = "archived.guest@example.com"
    )

    val beforeUpdate = AddGuestRequest(
        firstName = "Before",
        lastName = "Update",
        email = "before-${UUID.randomUUID()}@example.com"
    )

}
