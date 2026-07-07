package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId

interface Invitations {

    fun add(invitation: Invitation): Invitation
    fun findById(id: InvitationId): Invitation?
    fun list(criteria: InvitationListCriteria): InvitationPage
    fun findAssignedGuestIds(guestIds: Set<GuestId>): Set<GuestId>
    fun findInvitationByAccessToken(token: InvitationAccessToken): Invitation?
}

