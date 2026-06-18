package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.infrastructure.config.AuthRateLimitProperties
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class AuthRateLimiter(
    private val properties: AuthRateLimitProperties,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val requestsByClient = ConcurrentHashMap<String, Window>()

    fun check(clientKey: String): Decision {
        if (!properties.enabled) {
            return Decision(allowed = true, retryAfterSeconds = 0)
        }

        val now = Instant.now(clock)
        val windowDurationSeconds = properties.windowSeconds.coerceAtLeast(1)
        val maxRequests = properties.maxRequestsPerWindow.coerceAtLeast(1)

        val updated = requestsByClient.compute(clientKey) { _, current ->
            val refreshed = current?.takeIf { now.isBefore(it.windowEnd) }
                ?: Window(used = 0, windowEnd = now.plusSeconds(windowDurationSeconds))

            refreshed.copy(used = refreshed.used + 1)
        } ?: Window(used = 1, windowEnd = now.plusSeconds(windowDurationSeconds))

        if (updated.used <= maxRequests) {
            return Decision(allowed = true, retryAfterSeconds = 0)
        }

        val retryAfter = updated.windowEnd.epochSecond - now.epochSecond
        return Decision(allowed = false, retryAfterSeconds = retryAfter.coerceAtLeast(1))
    }
}

data class Decision(
    val allowed: Boolean,
    val retryAfterSeconds: Long,
)

private data class Window(
    val used: Int,
    val windowEnd: Instant,
)
