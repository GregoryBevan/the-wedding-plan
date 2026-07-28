package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class AddGuestCommand(
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: Language = Language.FR,
) {
    fun toGuest() =
        Guest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            language = language,
        )
}

