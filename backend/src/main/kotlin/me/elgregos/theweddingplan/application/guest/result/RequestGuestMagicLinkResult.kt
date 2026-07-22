package me.elgregos.theweddingplan.application.guest.result

sealed interface RequestGuestMagicLinkResult {
    data object Sent : RequestGuestMagicLinkResult
    data object InvitationNotFound : RequestGuestMagicLinkResult
    data object GuestNotFound : RequestGuestMagicLinkResult
    data object DeliveryFailed : RequestGuestMagicLinkResult
}

