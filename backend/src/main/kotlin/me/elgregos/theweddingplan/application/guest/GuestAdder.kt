package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.Guests
import org.springframework.stereotype.Service

@Service
class GuestAdder(private val guests: Guests) {
    fun add(firstName: String, lastName: String, email: String) =
        guests.add(Guest(firstName = firstName, lastName = lastName, email = email))
}
