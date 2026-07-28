package me.elgregos.theweddingplan.api.guest.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import kotlin.test.Test

class GuestResponseTest {

    @Test
    fun `should map guest to response`() {
            assertThat(johnDoe.toResponse()).isEqualTo(GuestResponseFixtures.johnDoe)
    }
}


