package me.elgregos.theweddingplan.api.guest

import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.application.guest.GuestLister
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.http.HttpStatus
import assertk.assertThat
import assertk.assertions.isEqualTo
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestEndpointTest {

    private lateinit var guestAdder: GuestAdder
    private lateinit var guestLister: GuestLister
    private lateinit var guestEndpoint: GuestEndpoint

    @BeforeTest
    fun setUp() {
        guestAdder = mockk()
        guestLister = mockk()
        guestEndpoint = GuestEndpoint(guestAdder, guestLister)
    }

    @Test
    fun `should map guest to response`() {
        assertThat(GuestFixtures.johnDoe.toResponse()).isEqualTo(GuestResponseFixtures.johnDoe)
    }

    @Test
    fun `should add a new guest`() {
        val request = mockk<ServerRequest>()
        val guest = GuestFixtures.johnDoe
        val addGuestRequest = AddGuestRequest(guest.firstName, guest.lastName, guest.email)

        every { request.body(AddGuestRequest::class.java) } returns addGuestRequest
        every { guestAdder.add(guest.firstName, guest.lastName, guest.email) } returns guest

        val response = guestEndpoint.addGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `should map guest page to response`() {
        val guestPage = GuestPage(
            items = listOf(GuestFixtures.johnDoe),
            page = 1,
            size = 5,
            totalItems = 9,
            totalPages = 2,
        )

        assertThat(guestPage.toResponse()).isEqualTo(
            GuestPageResponse(
                items = listOf(GuestResponseFixtures.johnDoe),
                page = 1,
                size = 5,
                totalItems = 9,
                totalPages = 2,
            )
        )
    }

    @Test
    fun `should list guests with default pagination`() {
        val request = mockk<ServerRequest>()
        val guestPage = GuestPage(
            items = listOf(GuestFixtures.johnDoe),
            page = 0,
            size = 20,
            totalItems = 1,
            totalPages = 1,
        )

        stubPaginationParams(request)
        every { guestLister.list(0, 20) } returns guestPage

        val response = guestEndpoint.listGuests(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return bad request when size is invalid`() {
        val request = mockk<ServerRequest>()

        stubPaginationParams(request, page = "0", size = "0")

        val response = guestEndpoint.listGuests(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    private fun stubPaginationParams(request: ServerRequest, page: String? = null, size: String? = null) {
        every { request.param("page") } returns Optional.ofNullable(page)
        every { request.param("size") } returns Optional.ofNullable(size)
    }
}
