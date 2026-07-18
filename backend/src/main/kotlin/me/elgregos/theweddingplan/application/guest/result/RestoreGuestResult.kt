package me.elgregos.theweddingplan.application.guest.result

import me.elgregos.theweddingplan.domain.guest.entity.Guest

sealed interface RestoreGuestResult {
    data class Restored(val guest: Guest) : RestoreGuestResult
    data object NotFound : RestoreGuestResult
    data object VersionConflict : RestoreGuestResult
}

