package me.elgregos.theweddingplan.domain.invitation

import java.util.UUID

@JvmInline
value class InvitationAccessToken(val value: String) {
    companion object {
        fun generate(): InvitationAccessToken = InvitationAccessToken("${UUID.randomUUID()}")
    }
}
