package me.elgregos.theweddingplan.api.guest.response

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestPage

data class GuestPageResponse(
    val items: List<GuestResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

internal fun GuestPage.toResponse() = GuestPageResponse(
    items = this.items.map(Guest::toResponse),
    page = this.page,
    size = this.size,
    totalItems = this.totalItems,
    totalPages = this.totalPages,
)
