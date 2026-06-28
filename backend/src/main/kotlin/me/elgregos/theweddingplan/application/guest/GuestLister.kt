package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.domain.guest.GuestListCriteria
import me.elgregos.theweddingplan.domain.guest.Guests
import org.springframework.stereotype.Service

@Service
class GuestLister(private val guests: Guests) {
    fun list(criteria: GuestListCriteria) = guests.list(criteria)
}

