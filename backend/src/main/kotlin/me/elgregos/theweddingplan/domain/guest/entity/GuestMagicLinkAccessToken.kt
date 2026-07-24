package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.shared.UUID_REGEX
import java.util.UUID

@JvmInline
value class GuestMagicLinkAccessToken(val value: String = "${UUID.randomUUID()}") {
    init {
        require(UUID_REGEX.matches(value)) { "Guest magic-link token $value is not a valid UUID" }
    }

    companion object {
        fun fromStringOrNull(value: String): GuestMagicLinkAccessToken? =
            value.takeIf(UUID_REGEX::matches)?.let(::GuestMagicLinkAccessToken)
    }
}

