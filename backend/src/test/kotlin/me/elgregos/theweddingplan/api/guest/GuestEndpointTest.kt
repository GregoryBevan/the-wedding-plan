package me.elgregos.theweddingplan.api.guest

import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.application.guest.GuestGetter
import me.elgregos.theweddingplan.application.guest.GuestLister
import me.elgregos.theweddingplan.application.guest.UpdateGuestResult
import me.elgregos.theweddingplan.application.guest.GuestUpdater
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.charlieDavis
import me.elgregos.theweddingplan.api.guest.UpdateGuestRequestFixtures.johnDoeUpdated as johnDoeUpdatedRequest
import me.elgregos.theweddingplan.domain.guest.GuestActiveFilter
import me.elgregos.theweddingplan.domain.guest.GuestListCriteria
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeUpdated
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
    private lateinit var guestGetter: GuestGetter
    private lateinit var guestUpdater: GuestUpdater
    private lateinit var guestEndpoint: GuestEndpoint

    @BeforeTest
    fun setUp() {
        guestAdder = mockk()
        guestLister = mockk()
        guestGetter = mockk()
        guestUpdater = mockk()
        guestEndpoint = GuestEndpoint(guestAdder, guestLister, guestGetter, guestUpdater)
    }

    @Test
    fun `should map guest to response`() {
        assertThat(johnDoe.toResponse()).isEqualTo(GuestResponseFixtures.johnDoe)
    }

    @Test
    fun `should add a new guest`() {
        val request = mockk<ServerRequest>()
        val guest = johnDoe

        every { request.body(AddGuestRequest::class.java) } returns charlieDavis
        every { guestAdder.add(charlieDavis.toCommand()) } returns guest

        val response = guestEndpoint.addGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `should map guest page to response`() {
        val guestPage = GuestPage(
            items = listOf(johnDoe),
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
            items = listOf(johnDoe),
            page = 0,
            size = 20,
            totalItems = 1,
            totalPages = 1,
        )

        stubPaginationParams(request)
        every { guestLister.list(GuestListCriteria(page = 0, size = 20, activeFilter = GuestActiveFilter.ACTIVE)) } returns guestPage

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

    @Test
    fun `should get guest by id`() {
        val request = mockk<ServerRequest>()
        val guest = johnDoe

        every { request.pathVariable("id") } returns guest.id.toString()
        every { guestGetter.get(guest.id) } returns guest

        val response = guestEndpoint.getGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return bad request for malformed guest id on get`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns "malformed-id"

        val response = guestEndpoint.getGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return not found for missing guest on get`() {
        val request = mockk<ServerRequest>()
        val guestId = GuestId.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99")

        every { request.pathVariable("id") } returns guestId.toString()
        every { guestGetter.get(guestId) } returns null

        val response = guestEndpoint.getGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should update guest by id`() {
        val request = mockk<ServerRequest>()
        val guest = johnDoe

        every { request.pathVariable("id") } returns guest.id.toString()
        every { request.body(UpdateGuestRequest::class.java) } returns johnDoeUpdatedRequest
        every {
            guestUpdater.update(guest.id, johnDoeUpdatedRequest.toCommand())
        } returns UpdateGuestResult.Updated(johnDoeUpdated)

        val response = guestEndpoint.updateGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return bad request for malformed guest id on update`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns "not-an-id"

        val response = guestEndpoint.updateGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return not found for missing guest on update`() {
        val request = mockk<ServerRequest>()
        val guestId = GuestId.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a98")

        every { request.pathVariable("id") } returns guestId.toString()
        every { request.body(UpdateGuestRequest::class.java) } returns johnDoeUpdatedRequest
        every {
            guestUpdater.update(guestId, johnDoeUpdatedRequest.toCommand())
        } returns UpdateGuestResult.NotFound

        val response = guestEndpoint.updateGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return conflict when version does not match on update`() {
        val request = mockk<ServerRequest>()
        val guestId = GuestId.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a98")
        val updateRequest = johnDoeUpdatedRequest

        every { request.pathVariable("id") } returns guestId.toString()
        every { request.body(UpdateGuestRequest::class.java) } returns updateRequest
        every {
            guestUpdater.update(guestId, updateRequest.toCommand())
        } returns UpdateGuestResult.VersionConflict

        val response = guestEndpoint.updateGuest(request)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CONFLICT)
    }

    private fun stubPaginationParams(request: ServerRequest, page: String? = null, size: String? = null) {
        every { request.param("page") } returns Optional.ofNullable(page)
        every { request.param("size") } returns Optional.ofNullable(size)
    }

}
