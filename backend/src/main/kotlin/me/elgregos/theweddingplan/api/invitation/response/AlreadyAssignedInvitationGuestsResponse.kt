package me.elgregos.theweddingplan.api.invitation.response

data class AlreadyAssignedInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)