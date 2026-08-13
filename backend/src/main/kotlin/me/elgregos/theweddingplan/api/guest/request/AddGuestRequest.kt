package me.elgregos.theweddingplan.api.guest.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import me.elgregos.theweddingplan.application.guest.command.AddGuestCommand
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class AddGuestRequest(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:NotBlank @field:Email
    val email: String,
    val language: String? = null,
) {
    internal fun toCommand(defaultLanguage: Language) =
        AddGuestCommand(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            language = Language.fromNullable(language, defaultLanguage)
        )
}
