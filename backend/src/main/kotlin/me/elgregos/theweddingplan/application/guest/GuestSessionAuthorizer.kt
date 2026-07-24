package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class GuestSessionAuthorizer(
    private val invitations: Invitations,
) {

    fun isGuestInInvitation(invitationId: InvitationId, guestId: GuestId): Boolean =
        invitations.findById(invitationId)?.guests?.any { it.id == guestId } == true
}

