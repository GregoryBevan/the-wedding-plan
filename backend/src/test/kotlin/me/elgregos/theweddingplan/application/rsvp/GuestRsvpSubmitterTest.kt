package me.elgregos.theweddingplan.application.rsvp

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommand
import me.elgregos.theweddingplan.application.rsvp.result.SubmitGuestRsvpResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpFixtures.johnDoeRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestRsvpSubmitterTest {

    private lateinit var guestRsvps: GuestRsvps
    private lateinit var guestRsvpSubmitter: GuestRsvpSubmitter

    @BeforeTest
    fun setUp() {
        guestRsvps = mockk()
        guestRsvpSubmitter = GuestRsvpSubmitter(guestRsvps)
    }

    @Test
    fun `should report a created rsvp when the guest has none`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.ATTENDING))

        assertThat(result).isInstanceOf(SubmitGuestRsvpResult.Created::class)
    }

    @Test
    fun `should create the rsvp at the initial version`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns null
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.ATTENDING))

        assertThat((result as SubmitGuestRsvpResult.Created).rsvp.version).isEqualTo(1L)
    }

    @Test
    fun `should report an updated rsvp when the guest already has one`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvp
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.DECLINED))

        assertThat(result).isInstanceOf(SubmitGuestRsvpResult.Updated::class)
    }

    @Test
    fun `should bump the version when updating an existing rsvp`() {
        every { guestRsvps.findByGuestId(johnDoe.id) } returns johnDoeRsvp
        every { guestRsvps.save(any()) } answers { firstArg() }

        val result = guestRsvpSubmitter.submit(SubmitGuestRsvpCommand(johnDoe.id, RsvpAttendance.DECLINED))

        assertThat((result as SubmitGuestRsvpResult.Updated).rsvp.version).isEqualTo(2L)
    }
}



