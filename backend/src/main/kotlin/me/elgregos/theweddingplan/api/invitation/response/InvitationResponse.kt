package me.elgregos.theweddingplan.api.invitation.response

import me.elgregos.theweddingplan.domain.invitation.entity.Invitation

data class InvitationResponse(
    val id: String,
    val accessToken: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val label: String,
    val description: String,
    val guests: List<InvitationGuestResponse>,
    val guestCount: Int,
)

internal fun Invitation.toResponse() = InvitationResponse(
    id = id.toString(),
    accessToken = accessToken.value,
    version = version,
    creationDate = creationDate.toString(),
    updateDate = updateDate.toString(),
    label = label,
    description = description,
    guests = guests
        .sortedBy { it.id.toString() }
        .map {
            InvitationGuestResponse(
                id = it.id.toString(),
                firstName = it.firstName,
                lastName = it.lastName,
                email = it.email,
            )
        },
    guestCount = guests.size,
)