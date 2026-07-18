package me.elgregos.theweddingplan.application.invitation.result

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation

sealed interface UpdateInvitationResult {
    data class Updated(val invitation: Invitation) : UpdateInvitationResult
    data object NotFound : UpdateInvitationResult
    data object VersionConflict : UpdateInvitationResult
    data object MissingGuests : UpdateInvitationResult
    data class InvalidGuests(val guestIds: Set<GuestId>) : UpdateInvitationResult
    data class AlreadyAssignedGuests(val guestIds: Set<GuestId>) : UpdateInvitationResult
}
