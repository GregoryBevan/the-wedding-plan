package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import kotlin.test.BeforeTest
import kotlin.test.Test

class NoopGuestMagicLinkSenderTest {

    private lateinit var noopGuestMagicLinkSender: NoopGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        noopGuestMagicLinkSender = NoopGuestMagicLinkSender()
    }

    @Test
    fun `should do nothing when sending magic link`() {
        assertThat(noopGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)).isEqualTo(Unit)
    }
}


