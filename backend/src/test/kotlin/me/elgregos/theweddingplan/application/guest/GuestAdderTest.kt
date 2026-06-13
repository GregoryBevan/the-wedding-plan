package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.Guests
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GuestAdderTest {

    private lateinit var guests: Guests
    private lateinit var guestAdder: GuestAdder

    @BeforeEach
    fun setUp() {
        guests = mockk()
        guestAdder = GuestAdder(guests)
    }

    @Test
    fun `should add a new guest`() {
        val guest = GuestFixtures.johnDoe
        every { guests.add(any()) } returns guest

        val result = guestAdder.add(guest.firstName, guest.lastName, guest.email)

        assertThat(result).isEqualTo(guest)
    }
}
