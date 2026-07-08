package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.shared.UUID_REGEX
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class InvitationId(val value: Uuid) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): InvitationId = InvitationId(Uuid.generateV7())
        fun fromString(uuid: String): InvitationId = InvitationId(Uuid.parse(uuid))
        fun fromStringOrNull(uuid: String): InvitationId? = uuid.takeIf(UUID_REGEX::matches)?.let(::fromString)
    }
}

