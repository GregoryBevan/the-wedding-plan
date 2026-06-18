package me.elgregos.theweddingplan.api.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import me.elgregos.theweddingplan.infrastructure.config.AuthRateLimitProperties
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test

class AuthRateLimiterTest {

    @Test
    fun `should allow requests within window limit`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = true, maxRequestsPerWindow = 2, windowSeconds = 60),
            clock = fixedClock(),
        )

        assertThat(limiter.check("127.0.0.1").allowed).isTrue()
        assertThat(limiter.check("127.0.0.1").allowed).isTrue()
    }

    @Test
    fun `should deny requests over window limit and provide retry after`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = true, maxRequestsPerWindow = 1, windowSeconds = 60),
            clock = fixedClock(),
        )

        limiter.check("127.0.0.1")
        val denied = limiter.check("127.0.0.1")

        assertThat(denied.allowed).isFalse()
        assertThat(denied.retryAfterSeconds).isEqualTo(60)
    }

    @Test
    fun `should allow requests when limiter is disabled`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = false),
            clock = fixedClock(),
        )

        repeat(100) {
            assertThat(limiter.check("127.0.0.1").allowed).isTrue()
        }
    }

    private fun fixedClock(): Clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
}
