package me.elgregos.theweddingplan.api.guest

object UpdateGuestRequestFixtures {
    val johnDoeUpdated = UpdateGuestRequest(
        version = 1L,
        firstName = "John Updated",
        lastName = "Doe Updated",
        email = "john.updated@example.com",
    )
}

