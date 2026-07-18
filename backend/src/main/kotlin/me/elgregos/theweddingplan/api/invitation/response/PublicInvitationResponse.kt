package me.elgregos.theweddingplan.api.invitation.response

import me.elgregos.theweddingplan.domain.invitation.entity.Invitation

data class PublicInvitationResponse(
    val label: String,
    val description: String,
    val guests: List<PublicInvitationGuestResponse>,
    val guestCount: Int,
)

internal fun Invitation.toPublicResponse() = PublicInvitationResponse(
    label = label,
    description = description,
    guests = guests
        .sortedBy { it.id.toString() }
        .map {
            PublicInvitationGuestResponse(
                id = it.id.toString(),
                firstName = it.firstName,
                lastName = it.lastName,
            )
        },
    guestCount = guests.size,
)