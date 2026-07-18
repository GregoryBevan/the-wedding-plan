package me.elgregos.theweddingplan.application.guest.command

import me.elgregos.theweddingplan.domain.guest.entity.GuestId

data class UpdateGuestCommand(
    val id: GuestId,
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
)

