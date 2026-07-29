package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.shared.UUID_REGEX
import java.security.MessageDigest
import java.util.UUID

@JvmInline
value class GuestMagicLinkAccessToken(val value: String = "${UUID.randomUUID()}") {
    init {
        require(UUID_REGEX.matches(value)) { "Guest magic-link token $value is not a valid UUID" }
    }

    /**
     * Digest under which the token is stored and matched at rest, so a database leak never exposes
     * usable bearer tokens. A plain SHA-256 (no salt / no work factor) is sufficient here: the token
     * is a high-entropy random UUID and is therefore not brute-forceable, unlike low-entropy secrets
     * such as passwords which require slow, salted hashing.
     */
    fun hashed(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }

    companion object {
        fun fromStringOrNull(value: String): GuestMagicLinkAccessToken? =
            value.takeIf(UUID_REGEX::matches)?.let(::GuestMagicLinkAccessToken)
    }
}

