package me.elgregos.theweddingplan.api.guest.request

import me.elgregos.theweddingplan.application.guest.command.AddGuestCommand
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class AddGuestRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: String? = null,
) {
    internal fun toCommand(defaultLanguage: Language) =
        AddGuestCommand(
            firstName = firstName,
            lastName = lastName,
            email = email,
            language = Language.fromNullable(language, defaultLanguage)
        )
}
