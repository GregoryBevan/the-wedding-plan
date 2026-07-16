package me.elgregos.theweddingplan.api.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.invitation.AddInvitationResult
import me.elgregos.theweddingplan.application.invitation.InvitationAdder
import me.elgregos.theweddingplan.application.invitation.InvitationGetter
import me.elgregos.theweddingplan.application.invitation.InvitationLister
import me.elgregos.theweddingplan.application.invitation.InvitationUpdater
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationResult
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.brideFamilyInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import me.elgregos.theweddingplan.domain.invitation.InvitationPage
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerRequest
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class InvitationEndpointTest {

    private lateinit var invitationAdder: InvitationAdder
    private lateinit var invitationLister: InvitationLister
    private lateinit var invitationGetter: InvitationGetter
    private lateinit var invitationUpdater: InvitationUpdater
    private lateinit var invitationEndpoint: InvitationEndpoint

    private val invitationGuestIds = brideFamilyInvitation.guests.map { it.id.toString() }

    @BeforeTest
    fun setUp() {
        invitationAdder = mockk()
        invitationLister = mockk()
        invitationGetter = mockk()
        invitationUpdater = mockk()
        invitationEndpoint = InvitationEndpoint(invitationAdder, invitationLister, invitationGetter, invitationUpdater)
    }

    private fun updateInvitationRequest(
        label: String = brideFamilyInvitation.label,
        description: String = brideFamilyInvitation.description,
        guestIds: List<String> = invitationGuestIds,
        version: Long = brideFamilyInvitation.version,
    ) = UpdateInvitationRequest(
        version = version,
        label = label,
        description = description,
        guestIds = guestIds,
    )

    private fun mockUpdateRequest(request: ServerRequest, invitationId: InvitationId, payload: UpdateInvitationRequest) {
        every { request.pathVariable("id") } returns invitationId.toString()
        every { request.body(UpdateInvitationRequest::class.java) } returns payload
    }

    @Test
    fun `should map invitation to response`() {
        val response = brideFamilyInvitation.toResponse()

        assertThat(response.accessToken).isEqualTo(brideFamilyInvitation.accessToken.value)
        assertThat(response.label).isEqualTo(brideFamilyInvitation.label)
        assertThat(response.description).isEqualTo(brideFamilyInvitation.description)
        assertThat(response.guests.map{it.id}).isEqualTo(brideFamilyInvitation.guests.map { "${it.id}" })
        assertThat(response.guestCount).isEqualTo(brideFamilyInvitation.guests.size)
    }

    @Test
    fun `should add invitation`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = brideFamilyInvitation.label,
            guestIds = brideFamilyInvitation.guests.map { it.id.toString() },
            description = brideFamilyInvitation.description
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload
        every { invitationAdder.add(payload.toCommandOrNull()!!) } returns AddInvitationResult.Added(brideFamilyInvitation)

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun `should return bad request when invitation has no guests`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = "No guests",
            guestIds = emptyList(),
            description =   "An invitation with no guests"
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload
        every { invitationAdder.add(payload.toCommandOrNull()!!) } returns AddInvitationResult.MissingGuests

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when invitation title is blank`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = "   ",
            guestIds = listOf(brideFamilyInvitation.guests.first().id.toString()),
            description =   "An invitation with no guests"
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when invitation guest id is malformed`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = "Family table",
            guestIds = listOf("not-a-uuid"),
            description =   "An invitation with invalid guest id"
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when invitation service reports invalid guests`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = brideFamilyInvitation.label,
            guestIds = brideFamilyInvitation.guests.map { it.id.toString() },
            description = brideFamilyInvitation.description
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload
        every { invitationAdder.add(payload.toCommandOrNull()!!) } returns AddInvitationResult.InvalidGuests(
            brideFamilyInvitation.guests.map { it.id }.toSet(),
        )

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return conflict when invitation service reports already assigned guests`() {
        val request = mockk<ServerRequest>()
        val payload = AddInvitationRequest(
            label = brideFamilyInvitation.label,
            guestIds = brideFamilyInvitation.guests.map { it.id.toString() },
            description = brideFamilyInvitation.description
        )

        every { request.body(AddInvitationRequest::class.java) } returns payload
        every { invitationAdder.add(payload.toCommandOrNull()!!) } returns AddInvitationResult.AlreadyAssignedGuests(
            brideFamilyInvitation.guests.map { it.id }.toSet(),
        )

        assertThat(invitationEndpoint.addInvitation(request).statusCode()).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `should list invitations with default pagination`() {
        val request = mockk<ServerRequest>()
        val invitationPage = InvitationPage(
            items = listOf(brideFamilyInvitation),
            page = 0,
            size = 20,
            totalItems = 1,
            totalPages = 1,
        )

        every { request.param("page") } returns Optional.empty()
        every { request.param("size") } returns Optional.empty()
        every { invitationLister.list(me.elgregos.theweddingplan.domain.invitation.InvitationListCriteria(page = 0, size = 20)) } returns invitationPage

        assertThat(invitationEndpoint.listInvitations(request).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return bad request when page query parameter is not numeric`() {
        val request = mockk<ServerRequest>()

        every { request.param("page") } returns Optional.of("abc")
        every { request.param("size") } returns Optional.empty()

        assertThat(invitationEndpoint.listInvitations(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when size query parameter is not numeric`() {
        val request = mockk<ServerRequest>()

        every { request.param("page") } returns Optional.empty()
        every { request.param("size") } returns Optional.of("abc")

        assertThat(invitationEndpoint.listInvitations(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when page is negative`() {
        val request = mockk<ServerRequest>()

        every { request.param("page") } returns Optional.of("-1")
        every { request.param("size") } returns Optional.of("20")

        assertThat(invitationEndpoint.listInvitations(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request when size is zero`() {
        val request = mockk<ServerRequest>()

        every { request.param("page") } returns Optional.of("0")
        every { request.param("size") } returns Optional.of("0")

        assertThat(invitationEndpoint.listInvitations(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should get invitation by id`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns brideFamilyInvitation.id.toString()
        every { invitationGetter.get(brideFamilyInvitation.id) } returns brideFamilyInvitation

        assertThat(invitationEndpoint.getInvitation(request).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return bad request for malformed invitation id on get`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns johnDoe.id.toString().replace('-', 'x')

        assertThat(invitationEndpoint.getInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return not found when invitation id does not exist on get`() {
        val request = mockk<ServerRequest>()
        val invitationId = InvitationId.fromString("019f5f72-cbb3-76fd-b237-5d61dcbaf707")

        every { request.pathVariable("id") } returns invitationId.toString()
        every { invitationGetter.get(invitationId) } returns null

        assertThat(invitationEndpoint.getInvitation(request).statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should update invitation`() {
        val request = mockk<ServerRequest>()
        val invitationId = brideFamilyInvitation.id
        val payload = updateInvitationRequest(
            label = "Updated family table",
            description = "Updated description",
        )

        mockUpdateRequest(request, invitationId, payload)
        every { invitationUpdater.update(payload.toCommandOrNull(invitationId)!!) } returns UpdateInvitationResult.Updated(
            brideFamilyInvitation.copy(label = "Updated family table", description = "Updated description")
        )

        assertThat(invitationEndpoint.updateInvitation(request).statusCode()).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should return conflict when update version conflicts`() {
        val request = mockk<ServerRequest>()
        val invitationId = brideFamilyInvitation.id
        val payload = updateInvitationRequest()

        mockUpdateRequest(request, invitationId, payload)
        every { invitationUpdater.update(payload.toCommandOrNull(invitationId)!!) } returns UpdateInvitationResult.VersionConflict

        assertThat(invitationEndpoint.updateInvitation(request).statusCode()).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `should return not found when update invitation id does not exist`() {
        val request = mockk<ServerRequest>()
        val invitationId = brideFamilyInvitation.id
        val payload = updateInvitationRequest()

        mockUpdateRequest(request, invitationId, payload)
        every { invitationUpdater.update(payload.toCommandOrNull(invitationId)!!) } returns UpdateInvitationResult.NotFound

        assertThat(invitationEndpoint.updateInvitation(request).statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return bad request when update label is blank`() {
        val request = mockk<ServerRequest>()
        val payload = updateInvitationRequest(label = "   ")

        mockUpdateRequest(request, brideFamilyInvitation.id, payload)

        assertThat(invitationEndpoint.updateInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return bad request for malformed invitation id on update`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns johnDoe.id.toString().replace('-', 'x')

        assertThat(invitationEndpoint.updateInvitation(request).statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}

