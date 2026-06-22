package me.elgregos.theweddingplan.domain.shared

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object Dates {
    fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

    fun nowUtcMillis(): LocalDateTime = nowUtc().truncatedTo(ChronoUnit.MILLIS)
}