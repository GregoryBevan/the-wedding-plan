package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.Guest

data class AddGuestCommand(
    val firstName: String,
    val lastName: String,
    val email: String,
) {
    fun toGuest() =
        Guest(
            firstName = firstName,
            lastName = lastName,
            email = email,
        )
}

