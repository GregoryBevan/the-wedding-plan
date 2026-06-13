package me.elgregos.theweddingplan.api.guest

import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.http.HttpStatus
import assertk.assertThat
import assertk.assertions.isEqualTo

class GuestEndpointTest {

    @Test
    fun `should map guest to response`() {
        assertThat(GuestFixtures.johnDoe.toResponse()).isEqualTo(GuestResponseFixtures.johnDoe)
    }

    @Test
    fun `should add a new guest`() {
        val guestAdder = mockk<GuestAdder>()
        val guestEndpoint = GuestEndpoint(guestAdder)
        val request = mockk<ServerRequest>()
        val guest = GuestFixtures.johnDoe
        val addGuestRequest = AddGuestRequest(guest.firstName, guest.lastName, guest.email)

        every { request.body(AddGuestRequest::class.java) } returns addGuestRequest
        every { guestAdder.add(guest.firstName, guest.lastName, guest.email) } returns guest

        val response = guestEndpoint.addGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
    }
}
