package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.invitation.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationTokenResolver(private val invitations: Invitations) {

    fun resolve(token: InvitationAccessToken) = invitations.findInvitationByAccessToken(token)
}

