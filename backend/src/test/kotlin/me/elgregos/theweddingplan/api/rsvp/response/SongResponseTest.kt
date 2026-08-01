package me.elgregos.theweddingplan.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.rsvp.response.SongResponseFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures
import kotlin.test.Test

class SongResponseTest {

    @Test
    fun `should map a song choice to a response`() {
        assertThat(GuestRsvpFixtures.laVieEnRose.toResponse()).isEqualTo(laVieEnRose)
    }
}

