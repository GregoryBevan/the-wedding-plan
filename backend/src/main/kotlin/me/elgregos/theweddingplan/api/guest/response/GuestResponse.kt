package me.elgregos.theweddingplan.api.guest.response

import me.elgregos.theweddingplan.domain.guest.entity.Guest

data class GuestResponse(
    val id: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val language: String,
)

internal fun Guest.toResponse() = GuestResponse(
    id = id.toString(),
    version = version,
    creationDate = creationDate.toString(),
    updateDate = updateDate.toString(),
    firstName = firstName,
    lastName = lastName,
    email = email,
    language = language.name,
)