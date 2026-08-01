package me.elgregos.theweddingplan.application.rsvp.result

import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp

sealed interface GetGuestRsvpResult {
    data class Submitted(val rsvp: GuestRsvp) : GetGuestRsvpResult
    data object NotSubmittedYet : GetGuestRsvpResult
}

