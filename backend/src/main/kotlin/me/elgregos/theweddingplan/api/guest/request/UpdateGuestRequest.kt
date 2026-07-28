package me.elgregos.theweddingplan.api.guest.request

import me.elgregos.theweddingplan.application.guest.command.UpdateGuestCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class UpdateGuestRequest(
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: String? = null,
) {
    internal fun toCommand(id: GuestId, defaultLanguage: Language) =
        UpdateGuestCommand(
            id = id,
            version = version,
            firstName = firstName,
            lastName = lastName,
            email = email,
            language = Language.fromNullable(language, defaultLanguage)
        )
}
