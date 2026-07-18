package me.elgregos.theweddingplan.application.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.repository.Guests
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestGetterTest {

    private lateinit var guests: Guests
    private lateinit var guestGetter: GuestGetter

    @BeforeTest
    fun setUp() {
        guests = mockk()
        guestGetter = GuestGetter(guests)
    }

    @Test
    fun `should get existing guest by id`() {
        val guest = GuestFixtures.johnDoe
        every { guests.findById(guest.id) } returns guest

        val result = guestGetter.get(guest.id)

        assertThat(result).isEqualTo(guest)
    }

    @Test
    fun `should return null when guest does not exist`() {
        val guestId = GuestFixtures.johnDoe.id
        every { guests.findById(guestId) } returns null

        val result = guestGetter.get(guestId)

        assertThat(result).isEqualTo(null)
    }
}

