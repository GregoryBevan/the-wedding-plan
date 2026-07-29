package me.elgregos.theweddingplan.domain.guest.entity

import assertk.assertThat
import assertk.assertions.hasLength
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.matches
import kotlin.test.Test

class GuestMagicLinkAccessTokenTest {

    private val token = GuestMagicLinkAccessToken("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")

    @Test
    fun `should hash to a 64-char lowercase hex sha-256 digest`() {
        val hash = token.hashed()

        assertThat(hash).hasLength(64)
        assertThat(hash).matches(Regex("[0-9a-f]{64}"))
    }

    @Test
    fun `should never expose the raw token in its digest`() {
        assertThat(token.hashed()).isNotEqualTo(token.value)
    }

    @Test
    fun `should be deterministic for the same token`() {
        assertThat(token.hashed()).isEqualTo(token.hashed())
    }

    @Test
    fun `should produce different digests for different tokens`() {
        assertThat(token.hashed())
            .isNotEqualTo(GuestMagicLinkAccessToken("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12").hashed())
    }
}

