package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation

sealed interface AddInvitationResult {
    data class Added(val invitation: Invitation) : AddInvitationResult
    data object MissingGuests : AddInvitationResult
    data class InvalidGuests(val guestIds: Set<GuestId>) : AddInvitationResult
}

