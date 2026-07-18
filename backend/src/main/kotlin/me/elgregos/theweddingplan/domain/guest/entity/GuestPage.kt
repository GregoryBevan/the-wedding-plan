package me.elgregos.theweddingplan.domain.guest.entity

data class GuestPage(
    val items: List<Guest>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

