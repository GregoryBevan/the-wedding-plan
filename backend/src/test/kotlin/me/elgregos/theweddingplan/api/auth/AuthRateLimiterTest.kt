package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.infrastructure.config.AuthRateLimitProperties
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthRateLimiterTest {

    @Test
    fun `should allow requests within window limit`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = true, maxRequestsPerWindow = 2, windowSeconds = 60),
            clock = fixedClock(),
        )

        assertTrue(limiter.check("127.0.0.1").allowed)
        assertTrue(limiter.check("127.0.0.1").allowed)
    }

    @Test
    fun `should deny requests over window limit and provide retry after`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = true, maxRequestsPerWindow = 1, windowSeconds = 60),
            clock = fixedClock(),
        )

        limiter.check("127.0.0.1")
        val denied = limiter.check("127.0.0.1")

        assertFalse(denied.allowed)
        assertEquals(60, denied.retryAfterSeconds)
    }

    @Test
    fun `should allow requests when limiter is disabled`() {
        val limiter = AuthRateLimiter(
            properties = AuthRateLimitProperties(enabled = false),
            clock = fixedClock(),
        )

        repeat(100) {
            assertTrue(limiter.check("127.0.0.1").allowed)
        }
    }

    private fun fixedClock(): Clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
}
