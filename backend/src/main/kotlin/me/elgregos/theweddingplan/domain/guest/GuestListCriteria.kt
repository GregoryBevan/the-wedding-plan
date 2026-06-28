package me.elgregos.theweddingplan.domain.guest

enum class GuestActiveFilter { ACTIVE, DELETED, ALL }

data class GuestListCriteria(
    val page: Int = 0,
    val size: Int = 20,
    val activeFilter: GuestActiveFilter = GuestActiveFilter.ACTIVE,
)
