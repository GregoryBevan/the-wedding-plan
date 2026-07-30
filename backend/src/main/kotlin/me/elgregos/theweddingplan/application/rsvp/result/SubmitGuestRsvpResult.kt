package me.elgregos.theweddingplan.application.rsvp.result

import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp

sealed interface SubmitGuestRsvpResult {
    data class Created(val rsvp: GuestRsvp) : SubmitGuestRsvpResult
    data class Updated(val rsvp: GuestRsvp) : SubmitGuestRsvpResult
}

