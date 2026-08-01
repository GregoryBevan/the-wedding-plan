package me.elgregos.theweddingplan.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.rsvp.response.GuestRsvpResponseFixtures.johnDoe
import me.elgregos.theweddingplan.api.rsvp.response.GuestRsvpResponseFixtures.johnDoeMealOnly
import me.elgregos.theweddingplan.api.rsvp.response.GuestRsvpResponseFixtures.johnDoeWithChoices
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpMealOnly
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvpWithChoices
import kotlin.test.Test

class GuestRsvpResponseTest {

    @Test
    fun `should map an rsvp without answers to a response`() {
        assertThat(johnDoeRsvp.toResponse()).isEqualTo(johnDoe)
    }

    @Test
    fun `should map an rsvp with meal and song to a response`() {
        assertThat(johnDoeRsvpWithChoices.toResponse()).isEqualTo(johnDoeWithChoices)
    }

    @Test
    fun `should map an rsvp with a meal but no song to a response`() {
        assertThat(johnDoeRsvpMealOnly.toResponse()).isEqualTo(johnDoeMealOnly)
    }
}

