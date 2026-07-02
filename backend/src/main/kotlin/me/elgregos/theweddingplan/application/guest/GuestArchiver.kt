package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import org.springframework.stereotype.Service

@Service
class GuestArchiver(private val guests: Guests) {

    fun archive(id: GuestId): ArchiveGuestResult =
        guests.findById(id)
            ?.let { existingGuest ->
                existingGuest.markAsArchived()
                    .let { guests.update(it, expectedVersion = existingGuest.version) }
                    ?.let(ArchiveGuestResult::Archived)
                    ?: ArchiveGuestResult.VersionConflict
            }
            ?: ArchiveGuestResult.NotFound
}

