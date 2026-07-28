package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.Language

data class UpdateGuestCommand(
    val id: GuestId,
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: Language = Language.FR,
)

