package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.Guest

sealed interface ArchiveGuestResult {
    data class Archived(val guest: Guest) : ArchiveGuestResult
    data object NotFound : ArchiveGuestResult
    data object VersionConflict : ArchiveGuestResult
}

