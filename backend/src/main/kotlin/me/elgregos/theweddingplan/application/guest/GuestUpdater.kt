package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.Guests
import org.springframework.stereotype.Service

@Service
class GuestUpdater(private val guests: Guests) {

    fun update(command: UpdateGuestCommand): UpdateGuestResult =
        with(command) {
            guests.findById(id)
                ?.let { existingGuest ->
                    when {
                        existingGuest.version != version -> UpdateGuestResult.VersionConflict
                        else -> existingGuest
                            .updateDetails(firstName, lastName, email)
                            .let { guests.update(it, expectedVersion = version) }
                            ?.let(UpdateGuestResult::Updated)
                            ?: UpdateGuestResult.VersionConflict
                    }
                }
                ?: UpdateGuestResult.NotFound
        }
}


