package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.application.guest.command.AddGuestCommand
import me.elgregos.theweddingplan.domain.guest.repository.Guests
import org.springframework.stereotype.Service

@Service
class GuestAdder(private val guests: Guests) {
    fun add(command: AddGuestCommand) =
        guests.add(command.toGuest())
}
