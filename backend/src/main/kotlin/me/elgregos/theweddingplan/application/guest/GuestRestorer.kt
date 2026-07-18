package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.application.guest.result.RestoreGuestResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestRestorer(private val guests: Guests) {

    fun restore(id: GuestId): RestoreGuestResult =
        guests.findArchivedById(id)
            ?.let { existingGuest ->
                existingGuest.restore()
                    .let { guests.restore(it, expectedVersion = existingGuest.version) }
                    ?.let(RestoreGuestResult::Restored)
                    ?: RestoreGuestResult.VersionConflict
            }
            ?: RestoreGuestResult.NotFound
}

