package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationTokenResolver(private val invitations: Invitations) {

    fun resolve(token: InvitationAccessToken) = invitations.findInvitationByAccessToken(token)
}

