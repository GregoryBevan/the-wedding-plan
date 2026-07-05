package me.elgregos.theweddingplan.domain.guest

enum class GuestStatus { ACTIVE, ARCHIVED, ALL }

data class GuestListCriteria(
    val page: Int = 0,
    val size: Int = 20,
    val status: GuestStatus = GuestStatus.ACTIVE,
    val search: String? = null,
)
