package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import org.springframework.stereotype.Service

@Service
class GuestGetter(private val guests: Guests) {
    fun get(id: GuestId) = guests.findById(id)
}

