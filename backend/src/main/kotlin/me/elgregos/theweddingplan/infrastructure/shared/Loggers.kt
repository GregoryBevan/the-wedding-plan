package me.elgregos.theweddingplan.infrastructure.shared

import io.github.oshai.kotlinlogging.KLogger

/**
 * Logs a non-identifying [message] at the primary level and defers any identifier-bearing [details]
 * to DEBUG, so operators keep a diagnosable event without leaking IDs in production logs. The
 * [details] lambda is only evaluated when DEBUG is enabled.
 */
fun KLogger.infoWithDetails(message: String, details: () -> Any?) {
    info { message }
    debug(details)
}

/** @see infoWithDetails */
fun KLogger.warnWithDetails(throwable: Throwable? = null, message: String, details: () -> Any?) {
    warn(throwable) { message }
    debug(details)
}


