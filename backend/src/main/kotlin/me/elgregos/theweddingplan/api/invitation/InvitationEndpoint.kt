package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.application.invitation.AddInvitationCommand
import me.elgregos.theweddingplan.application.invitation.AddInvitationResult
import me.elgregos.theweddingplan.application.invitation.InvitationAdder
import me.elgregos.theweddingplan.application.invitation.InvitationGetter
import me.elgregos.theweddingplan.application.invitation.InvitationLister
import me.elgregos.theweddingplan.application.invitation.InvitationUpdater
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationCommand
import me.elgregos.theweddingplan.application.invitation.UpdateInvitationResult
import me.elgregos.theweddingplan.api.common.intQueryParam
import me.elgregos.theweddingplan.api.common.invitationIdPathParam
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.InvitationId
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
    private val invitationUpdater: InvitationUpdater,
) {
    fun listInvitations(request: ServerRequest): ServerResponse =
        request.intQueryParam("page", default = 0)?.takeIf { it >= 0 }?.let { page ->
            request.intQueryParam("size", default = 20)?.takeIf { it > 0 }?.let { size ->
                ServerResponse.ok()
                    .body(invitationLister.list(InvitationListCriteria(page = page, size = size)).toResponse())
            }
        } ?: ServerResponse.badRequest().build()

    fun addInvitation(request: ServerRequest): ServerResponse =
        request.body(AddInvitationRequest::class.java)
            .toCommandOrNull()
            ?.let { command ->
                when (val result = invitationAdder.add(command)) {
                    is AddInvitationResult.Added -> ServerResponse.status(HttpStatus.CREATED)
                        .body(result.invitation.toResponse())

                    is AddInvitationResult.MissingGuests -> ServerResponse.badRequest().body(
                        MissingInvitationGuestsResponse(message = "At least one guest is required.")
                    )

                    is AddInvitationResult.InvalidGuests -> ServerResponse.badRequest().body(
                        InvalidInvitationGuestsResponse(
                            message = "Some guests were not found or are archived.",
                            guestIds = result.guestIds.map(GuestId::toString).sorted(),
                        )
                    )

                    is AddInvitationResult.AlreadyAssignedGuests -> ServerResponse.status(HttpStatus.CONFLICT).body(
                        AlreadyAssignedInvitationGuestsResponse(
                            message = "Some guests are already assigned to another invitation.",
                            guestIds = result.guestIds.map(GuestId::toString).sorted(),
                        )
                    )
                }
            } ?: ServerResponse.badRequest().build()

    fun getInvitation(request: ServerRequest): ServerResponse =
        request.invitationIdPathParam()?.let { id ->
            invitationGetter.get(id)
                ?.toResponse()
                ?.let(ServerResponse.ok()::body)
                ?: ServerResponse.notFound().build()
        } ?: ServerResponse.badRequest().build()

    fun updateInvitation(request: ServerRequest): ServerResponse =
        request.invitationIdPathParam()?.let { id ->
            request.body(UpdateInvitationRequest::class.java)
                .toCommandOrNull(id)
                ?.let { command ->
                    when (val result = invitationUpdater.update(command)) {
                        is UpdateInvitationResult.Updated -> ServerResponse.ok().body(result.invitation.toResponse())
                        is UpdateInvitationResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT)
                            .body(mapOf("message" to "This invitation has been modified elsewhere. Please reload and try again."))
                        is UpdateInvitationResult.NotFound -> ServerResponse.notFound().build()
                        is UpdateInvitationResult.MissingGuests -> ServerResponse.badRequest().body(
                            MissingInvitationGuestsResponse(message = "At least one guest is required.")
                        )

                        is UpdateInvitationResult.InvalidGuests -> ServerResponse.badRequest().body(
                            InvalidInvitationGuestsResponse(
                                message = "Some guests were not found or are archived.",
                                guestIds = result.guestIds.map(GuestId::toString).sorted(),
                            )
                        )

                        is UpdateInvitationResult.AlreadyAssignedGuests -> ServerResponse.status(HttpStatus.CONFLICT)
                            .body(
                                AlreadyAssignedInvitationGuestsResponse(
                                    message = "Some guests are already assigned to another invitation.",
                                    guestIds = result.guestIds.map(GuestId::toString).sorted(),
                                )
                            )
                    }
                } ?: ServerResponse.badRequest().build()
        } ?: ServerResponse.badRequest().build()
}


data class InvitationResponse(
    val id: String,
    val accessToken: String,
    val version: Long,
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

data class UpdateInvitationRequest(
    val version: Long,
    val label: String,
    val description: String,
    val guestIds: List<String>,
)

data class InvalidInvitationGuestsResponse(
    val message: String,
    val guestIds: List<String>,
)

data class AlreadyAssignedInvitationGuestsResponse(
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

internal fun UpdateInvitationRequest.toCommandOrNull(id: InvitationId): UpdateInvitationCommand? {
    val normalizedLabel = label.trim()
    if (normalizedLabel.isEmpty()) return null

    val parsedGuestIds = guestIds
        .map(String::trim)
        .filter(String::isNotEmpty)
        .map { runCatching { GuestId.fromString(it) }.getOrNull() ?: return null }
        .toSet()

    return UpdateInvitationCommand(
        id = id,
        version = version,
        label = normalizedLabel,
        description = description.trim(),
        guestIds = parsedGuestIds,
    )
}

internal fun Invitation.toResponse() = InvitationResponse(
    id = id.toString(),
    accessToken = accessToken.value,
    version = version,
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

