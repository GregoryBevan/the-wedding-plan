package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import kotlin.test.Test

class JwtGuestSessionTokensTest {

    private val guestSession = GuestSession(guestId = janeDoe.id, invitationId = bridesMaidInvitation.id)

    @Test
    fun `should issue a token that verifies back to the same guest session`() {
        val jwtGuestSessionTokens = JwtGuestSessionTokens(GuestAccessProperties())

        val token = jwtGuestSessionTokens.issue(guestSession)

        assertThat(jwtGuestSessionTokens.verify(token)).isEqualTo(guestSession)
    }

    @Test
    fun `should return null when token is malformed`() {
        val jwtGuestSessionTokens = JwtGuestSessionTokens(GuestAccessProperties())

        assertThat(jwtGuestSessionTokens.verify("not-a-jwt")).isNull()
    }

    @Test
    fun `should return null when token is signed with another secret`() {
        val issuer = JwtGuestSessionTokens(GuestAccessProperties(jwtSecret = "issuer-secret-key-0123456789-abcdefgh"))
        val verifier = JwtGuestSessionTokens(GuestAccessProperties(jwtSecret = "another-secret-key-0123456789-abcdefgh"))

        val token = issuer.issue(guestSession)

        assertThat(verifier.verify(token)).isNull()
    }
}


