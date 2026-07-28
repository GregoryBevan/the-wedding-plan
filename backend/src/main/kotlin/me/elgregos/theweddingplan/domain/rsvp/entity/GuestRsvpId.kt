package me.elgregos.theweddingplan.domain.rsvp.entity

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class GuestRsvpId(val value: Uuid = Uuid.generateV7()) {
    override fun toString(): String = value.toString()

    companion object {
        fun fromString(uuid: String): GuestRsvpId = GuestRsvpId(Uuid.parse(uuid))
    }
}

