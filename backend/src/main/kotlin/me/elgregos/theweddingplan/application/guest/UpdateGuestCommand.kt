package me.elgregos.theweddingplan.application.guest

data class UpdateGuestCommand(
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
)

