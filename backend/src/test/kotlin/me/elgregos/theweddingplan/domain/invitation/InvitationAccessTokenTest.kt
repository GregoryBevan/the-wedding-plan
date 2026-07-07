package me.elgregos.theweddingplan.domain.invitation

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.malformedToken
import kotlin.test.Test

class InvitationAccessTokenTest {

    @Test
    fun `should create token when value is a valid uuid`() {
        val value = "dca71c6f-4b29-43a0-80df-426786ca9075"

        assertThat(InvitationAccessToken(value).value).isEqualTo(value)
    }

    @Test
    fun `should fail when value is not a valid uuid`() {
        assertFailure { InvitationAccessToken(malformedToken) }
            .isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Invitation access token $malformedToken is not a valid UUID")
    }

    @Test
    fun `should parse valid token with fromStringOrNull`() {
        val value = "35cae9dd-4e1a-4a01-95c0-4c286cafc3e4"

        assertThat(InvitationAccessToken.fromStringOrNull(value)).isEqualTo(InvitationAccessToken(value))
    }

    @Test
    fun `should return null when parsing invalid token with fromStringOrNull`() {
        assertThat(InvitationAccessToken.fromStringOrNull(malformedToken)).isEqualTo(null)
    }
}

