package me.elgregos.theweddingplan.domain.invitation

interface Invitations {

    fun add(invitation: Invitation): Invitation
    fun findById(id: InvitationId): Invitation?
    fun list(criteria: InvitationListCriteria): InvitationPage
}

