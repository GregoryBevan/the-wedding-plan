package me.elgregos.theweddingplan.domain.shared

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.test.Test

class DatesTest {

    @Test
    fun `should return a utc now`() {
        val lowerBound = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1)

        val nowUtc = Dates.nowUtc()

        val upperBound = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)
        assertThat(nowUtc.isBefore(lowerBound)).isEqualTo(false)
        assertThat(nowUtc.isAfter(upperBound)).isEqualTo(false)
    }

    @Test
    fun `should return a utc now truncated to millis`() {
        val nowUtcMillis = Dates.nowUtcMillis()

        assertThat(nowUtcMillis).isEqualTo(nowUtcMillis.truncatedTo(ChronoUnit.MILLIS))
        assertThat(nowUtcMillis.nano % 1_000_000).isEqualTo(0)
    }
}