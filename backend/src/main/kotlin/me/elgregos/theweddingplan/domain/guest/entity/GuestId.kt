package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.shared.UUID_REGEX
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class GuestId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): GuestId = GuestId(Uuid.generateV7())
        fun fromString(uuid: String): GuestId = GuestId(Uuid.parse(uuid))
        fun fromStringOrNull(uuid: String): GuestId? = uuid.takeIf(UUID_REGEX::matches)?.let(::fromString)
    }
}
