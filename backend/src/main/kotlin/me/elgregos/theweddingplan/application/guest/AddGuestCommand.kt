package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.Guest

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

