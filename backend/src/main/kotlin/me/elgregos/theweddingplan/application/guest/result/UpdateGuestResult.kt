package me.elgregos.theweddingplan.application.guest.result

import me.elgregos.theweddingplan.domain.guest.entity.Guest

sealed interface UpdateGuestResult {
    data class Updated(val guest: Guest) : UpdateGuestResult
    data object NotFound : UpdateGuestResult
    data object VersionConflict : UpdateGuestResult
}

