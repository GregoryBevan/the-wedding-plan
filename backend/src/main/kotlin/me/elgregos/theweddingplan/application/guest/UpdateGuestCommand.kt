package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.GuestId

data class UpdateGuestCommand(
    val id: GuestId,
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
)

