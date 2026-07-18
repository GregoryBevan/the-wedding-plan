package me.elgregos.theweddingplan.domain.guest.entity

enum class GuestStatus { ACTIVE, ARCHIVED, ALL }
enum class GuestAvailability { ALL, UNASSIGNED }

data class GuestListCriteria(
    val page: Int = 0,
    val size: Int = 20,
    val status: GuestStatus = GuestStatus.ACTIVE,
    val availability: GuestAvailability = GuestAvailability.ALL,
    val search: String? = null,
)
