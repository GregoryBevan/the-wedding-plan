package me.elgregos.theweddingplan.api.rsvp.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import kotlin.test.Test

class GuestRsvpResponseTest {

    @Test
    fun `should map rsvp to response`() {
        assertThat(johnDoeRsvp.toResponse()).isEqualTo(
            GuestRsvpResponse(
                id = "${johnDoeRsvp.id}",
                version = johnDoeRsvp.version,
                creationDate = "${johnDoeRsvp.creationDate}",
                updateDate = "${johnDoeRsvp.updateDate}",
                attendance = johnDoeRsvp.attendance.name,
            )
        )
    }
}

