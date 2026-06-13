package me.elgregos.theweddingplan.domain.guest

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class GuestId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): GuestId = GuestId(Uuid.generateV7())
        fun fromString(uuid: String): GuestId = GuestId(Uuid.parse(uuid))
    }
}
