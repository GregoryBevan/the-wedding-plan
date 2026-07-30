package me.elgregos.theweddingplan.domain.rsvp.repository

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp

interface GuestRsvps {

    fun findByGuestId(guestId: GuestId): GuestRsvp?
    fun save(rsvp: GuestRsvp): GuestRsvp
}

