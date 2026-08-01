package me.elgregos.theweddingplan.application.rsvp

import me.elgregos.theweddingplan.application.rsvp.result.GetGuestRsvpResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class GuestRsvpGetter(private val guestRsvps: GuestRsvps) {

    fun get(guestId: GuestId): GetGuestRsvpResult =
        guestRsvps.findByGuestId(guestId)
            ?.let(GetGuestRsvpResult::Submitted)
            ?: GetGuestRsvpResult.NotSubmittedYet
}

