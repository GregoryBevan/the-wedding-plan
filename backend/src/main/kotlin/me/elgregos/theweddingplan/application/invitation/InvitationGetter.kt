package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationGetter(private val invitations: Invitations) {

    fun get(id: InvitationId) = invitations.findById(id)
}
