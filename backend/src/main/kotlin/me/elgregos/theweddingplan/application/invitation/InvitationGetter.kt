package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.invitation.InvitationId
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationGetter(private val invitations: Invitations) {

    fun get(id: InvitationId) = invitations.findById(id)
}
