package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestGetter(private val guests: Guests) {
    fun get(id: GuestId) = guests.findById(id)
}

