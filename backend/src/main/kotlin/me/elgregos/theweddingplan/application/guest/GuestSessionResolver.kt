package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.application.guest.result.GuestSessionResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class GuestSessionResolver(private val invitations: Invitations) {

    fun resolve(session: GuestSession): GuestSessionResult {
        val invitation = invitations.findById(session.invitationId)
            ?: return GuestSessionResult.InvitationNotFound

        return invitation.guests
            .firstOrNull { it.id == session.guestId }
            ?.let { GuestSessionResult.Resolved(it) }
            ?: GuestSessionResult.GuestNotInInvitation
    }
}

