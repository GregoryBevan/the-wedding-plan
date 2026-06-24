package me.elgregos.theweddingplan.application.guest

object UpdateGuestCommandFixtures {
    val johnDoeUpdated = UpdateGuestCommand(
        version = 1L,
        firstName = "John Updated",
        lastName = "Doe Updated",
        email = "john.updated@example.com",
    )
}

