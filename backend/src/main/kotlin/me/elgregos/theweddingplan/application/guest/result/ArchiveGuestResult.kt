package me.elgregos.theweddingplan.application.guest.result

import me.elgregos.theweddingplan.domain.guest.entity.Guest

sealed interface ArchiveGuestResult {
    data class Archived(val guest: Guest) : ArchiveGuestResult
    data object NotFound : ArchiveGuestResult
    data object VersionConflict : ArchiveGuestResult
}