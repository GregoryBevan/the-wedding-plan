package me.elgregos.theweddingplan.domain.invitation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class InvitationId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): InvitationId = InvitationId(Uuid.generateV7())
        fun fromString(uuid: String): InvitationId = InvitationId(Uuid.parse(uuid))
    }
}

