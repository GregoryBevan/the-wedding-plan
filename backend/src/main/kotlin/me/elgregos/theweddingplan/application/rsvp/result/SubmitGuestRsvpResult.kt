package me.elgregos.theweddingplan.application.rsvp.result

import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp

sealed interface SubmitGuestRsvpResult {
    val rsvp: GuestRsvp

    data class Created(override val rsvp: GuestRsvp) : SubmitGuestRsvpResult
    data class Updated(override val rsvp: GuestRsvp) : SubmitGuestRsvpResult
}

