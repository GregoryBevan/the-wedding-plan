package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationLister(private val invitations: Invitations) {

    fun list(criteria: InvitationListCriteria) = invitations.list(criteria)
}
