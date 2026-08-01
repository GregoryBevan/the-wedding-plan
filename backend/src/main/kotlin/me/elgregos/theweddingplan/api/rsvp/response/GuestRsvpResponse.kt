package me.elgregos.theweddingplan.api.rsvp.response

import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp

data class GuestRsvpResponse(
    val id: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val attendance: String,
    val meal: String? = null,
    val song: SongResponse? = null,
)

internal fun GuestRsvp.toResponse() = GuestRsvpResponse(
    id = id.toString(),
    version = version,
    creationDate = creationDate.toString(),
    updateDate = updateDate.toString(),
    attendance = attendance.name,
    meal = answers?.meal?.name,
    song = answers?.song?.toResponse(),
)

