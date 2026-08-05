package me.elgregos.theweddingplan.domain.song

/**
 * Raised when the shared wedding playlist cannot be reached or the provider returns an error,
 * so callers can isolate the failure (e.g. keep the guest's RSVP successful) instead of leaking a 500.
 */
class WeddingPlaylistUnavailableException : RuntimeException {
    constructor(cause: Throwable) : super("The wedding playlist is currently unavailable", cause)
    constructor(detail: String) : super("The wedding playlist is currently unavailable: $detail")
}

