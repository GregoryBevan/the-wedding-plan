package me.elgregos.theweddingplan.domain.invitation.entity

data class InvitationPage(
    val items: List<Invitation>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

