package me.elgregos.theweddingplan.api.guest.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import me.elgregos.theweddingplan.application.guest.command.UpdateGuestCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class UpdateGuestRequest(
    @field:PositiveOrZero
    val version: Long,
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:NotBlank @field:Email
    val email: String,
    val language: String? = null,
) {
    internal fun toCommand(id: GuestId) =
        UpdateGuestCommand(
            id = id,
            version = version,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            language = Language.parseOrNull(language)
        )
}
