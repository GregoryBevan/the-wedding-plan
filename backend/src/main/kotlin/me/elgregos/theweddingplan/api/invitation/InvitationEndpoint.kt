package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.application.invitation.AddInvitationCommand
import me.elgregos.theweddingplan.application.invitation.AddInvitationResult
import me.elgregos.theweddingplan.application.invitation.InvitationAdder
import me.elgregos.theweddingplan.application.invitation.InvitationGetter
import me.elgregos.theweddingplan.application.invitation.InvitationLister
import me.elgregos.theweddingplan.api.common.intQueryParam
import me.elgregos.theweddingplan.api.common.invitationIdPathParam
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.InvitationPage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class InvitationEndpoint(
    private val invitationAdder: InvitationAdder,
    private val invitationLister: InvitationLister,
    private val invitationGetter: InvitationGetter,
) {
    fun listInvitations(request: ServerRequest): ServerResponse {
        val page = request.intQueryParam("page", default = 0) ?: return ServerResponse.badRequest().build()
        val size = request.intQueryParam("size", default = 20) ?: return ServerResponse.badRequest().build()

        return if (page < 0 || size <= 0) {
            ServerResponse.badRequest().build()
        } else {
            ServerResponse.ok().body(invitationLister.list(InvitationListCriteria(page = page, size = size)).toResponse())
        }
    }

    fun addInvitation(request: ServerRequest): ServerResponse {
        val payload = request.body(AddInvitationRequest::class.java)
        val command = payload.toCommandOrNull() ?: return ServerResponse.badRequest().build()

        return when (val result = invitationAdder.add(command)) {
            is AddInvitationResult.Added -> ServerResponse.status(HttpStatus.CREATED).body(result.invitation.toResponse())
            AddInvitationResult.MissingGuests -> ServerResponse.badRequest().body(
                MissingInvitationGuestsResponse(message = "At least one guest is required.")
            )
            is AddInvitationResult.InvalidGuests -> ServerResponse.badRequest().body(
                InvalidInvitationGuestsResponse(
                    message = "Some guests were not found or are archived.",
                    guestIds = result.guestIds.map(GuestId::toString).sorted(),
                )
            )
        }
    }

    fun getInvitation(request: ServerRequest): ServerResponse {
        val id = request.invitationIdPathParam() ?: return ServerResponse.badRequest().build()

        return invitationGetter.get(id)
            ?.toResponse()
            ?.let(ServerResponse.ok()::body)
            ?: ServerResponse.notFound().build()
    }
}


data class InvitationResponse(
    val id: String,
    val creationDate: String,
    val updateDate: String,
    val label: String,
    val description: String,
    val guests: List<InvitationGuestResponse>,
    val guestCount: Int,
)

data class InvitationGuestResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
)

data class InvitationPageResponse(
    val items: List<InvitationResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

data class AddInvitationRequest(
    val label: String,
    val description: String,
    val guestIds: List<String>,
)

data class InvalidInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)

data class MissingInvitationGuestsResponse(val message: String)

internal fun AddInvitationRequest.toCommandOrNull(): AddInvitationCommand? {
    val normalizedLabel = label.trim()
    if (normalizedLabel.isEmpty()) return null

    val parsedGuestIds = guestIds
        .map(String::trim)
        .filter(String::isNotEmpty)
        .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
        .toSet()


    return AddInvitationCommand(
        label = normalizedLabel,
        description = description.trim(),
        guestIds = parsedGuestIds,
    )
}

internal fun Invitation.toResponse() = InvitationResponse(
    id = id.toString(),
    creationDate = creationDate.toString(),
    updateDate = updateDate.toString(),
    label = label,
    description = description,
    guests = guests
        .sortedBy { it.id.toString() }
        .map {
            InvitationGuestResponse(
                id = it.id.toString(),
                firstName = it.firstName,
                lastName = it.lastName,
                email = it.email,
            )
        },
    guestCount = guests.size,
)

internal fun InvitationPage.toResponse() = InvitationPageResponse(
    items = items.map(Invitation::toResponse),
    page = page,
    size = size,
    totalItems = totalItems,
    totalPages = totalPages,
)

