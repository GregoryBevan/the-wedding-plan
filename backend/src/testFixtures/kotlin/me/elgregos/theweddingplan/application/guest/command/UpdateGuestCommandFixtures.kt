package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures

object UpdateGuestCommandFixtures {
    val johnDoeUpdated = UpdateGuestCommand(
        id = GuestFixtures.johnDoe.id,
        version = 1L,
        firstName = "John Updated",
        lastName = "Doe Updated",
        email = "john.updated@example.com",
    )
}

