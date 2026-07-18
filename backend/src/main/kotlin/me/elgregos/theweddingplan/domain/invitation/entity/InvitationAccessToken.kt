package me.elgregos.theweddingplan.domain.invitation.entity

import me.elgregos.theweddingplan.domain.shared.UUID_REGEX
import java.util.UUID

@JvmInline
value class InvitationAccessToken(val value: String) {
    init {
        require(UUID_REGEX.matches(value)) { "Invitation access token $value is not a valid UUID" }
    }

    companion object {

        fun fromStringOrNull(value: String): InvitationAccessToken? =
            value.takeIf(UUID_REGEX::matches)?.let(::InvitationAccessToken)

        fun generate(): InvitationAccessToken = InvitationAccessToken("${UUID.randomUUID()}")
    }
}
