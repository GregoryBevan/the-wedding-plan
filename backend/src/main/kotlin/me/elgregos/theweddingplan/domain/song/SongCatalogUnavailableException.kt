package me.elgregos.theweddingplan.domain.song

/**
 * Raised when the underlying music catalog cannot be reached or returns an error,
 * so callers can surface a clean gateway error instead of leaking a 500.
 */
class SongCatalogUnavailableException : RuntimeException {
    constructor(cause: Throwable) : super("The song catalog is currently unavailable", cause)
    constructor(detail: String) : super("The song catalog is currently unavailable: $detail")
}

