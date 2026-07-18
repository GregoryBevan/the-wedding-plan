package me.elgregos.theweddingplan.domain.invitation.entity

import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.friendsInvitation

object InvitationPageFixtures {
    val firstPage = InvitationPage(
        items = listOf(brideFamilyInvitation, friendsInvitation),
        page = 0,
        size = 20,
        totalItems = 2,
        totalPages = 1,
    )
}

