package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.api.common.intQueryParam
import me.elgregos.theweddingplan.api.common.invitationIdPathParam
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequest
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequest
import me.elgregos.theweddingplan.api.invitation.response.AlreadyAssignedInvitationGuestsResponse
import me.elgregos.theweddingplan.api.invitation.response.InvalidInvitationGuestsResponse
import me.elgregos.theweddingplan.api.invitation.response.MissingInvitationGuestsResponse
import me.elgregos.theweddingplan.api.invitation.response.toResponse
import me.elgregos.theweddingplan.application.invitation.InvitationAdder
import me.elgregos.theweddingplan.application.invitation.InvitationGetter
import me.elgregos.theweddingplan.application.invitation.InvitationLister
import me.elgregos.theweddingplan.application.invitation.InvitationUpdater
import me.elgregos.theweddingplan.application.invitation.result.AddInvitationResult
import me.elgregos.theweddingplan.application.invitation.result.UpdateInvitationResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationListCriteria
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



