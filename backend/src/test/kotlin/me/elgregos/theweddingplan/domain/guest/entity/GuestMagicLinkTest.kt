package me.elgregos.theweddingplan.domain.guest.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import kotlin.test.Test

class GuestMagicLinkTest {

    @Test
    fun `should build guest access path from magic-link`() {
        val path = bridesMaidToJane.guestAccessPath()

        assertThat(path).isEqualTo(
            "/guest-access/${bridesMaidToJane.invitationAccessToken.value}/guests/${bridesMaidToJane.guestId}"
        )
    }
}

