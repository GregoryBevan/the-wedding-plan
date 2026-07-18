package me.elgregos.theweddingplan.api.invitation.response

const val MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE = "If the request is valid, you will receive an email shortly."

data class GuestAccessMagicLinkResponse(
    val message: String,
)