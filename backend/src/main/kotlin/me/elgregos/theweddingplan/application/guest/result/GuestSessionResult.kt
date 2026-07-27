package me.elgregos.theweddingplan.application.guest.result

import me.elgregos.theweddingplan.domain.guest.entity.Guest

sealed interface GuestSessionResult {

    data class Resolved(val guest: Guest) : GuestSessionResult

    data object InvitationNotFound : GuestSessionResult

    data object GuestNotInInvitation : GuestSessionResult
}

