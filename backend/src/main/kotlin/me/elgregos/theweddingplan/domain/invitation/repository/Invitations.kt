package me.elgregos.theweddingplan.domain.invitation.repository

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationPage

interface Invitations {

    fun add(invitation: Invitation): Invitation
    fun update(invitation: Invitation): Invitation?
    fun findById(id: InvitationId): Invitation?
    fun list(criteria: InvitationListCriteria): InvitationPage
    fun findAssignedGuestIds(guestIds: Set<GuestId>): Set<GuestId>
    fun findInvitationByAccessToken(token: InvitationAccessToken): Invitation?
}