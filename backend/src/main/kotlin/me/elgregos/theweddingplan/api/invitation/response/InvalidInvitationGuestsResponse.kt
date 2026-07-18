package me.elgregos.theweddingplan.api.invitation.response

data class InvalidInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)