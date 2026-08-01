package me.elgregos.theweddingplan.api.rsvp

import me.elgregos.theweddingplan.api.common.requireGuestSession
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequest
import me.elgregos.theweddingplan.api.rsvp.response.toResponse
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpGetter
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpSubmitter
import me.elgregos.theweddingplan.application.rsvp.result.GetGuestRsvpResult
import me.elgregos.theweddingplan.application.rsvp.result.SubmitGuestRsvpResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestRsvpEndpoint(
    private val guestRsvpSubmitter: GuestRsvpSubmitter,
    private val guestRsvpGetter: GuestRsvpGetter,
) {

    fun submit(request: ServerRequest): ServerResponse {
        val guestId = request.requireGuestSession().guestId
        val command = request.body(SubmitRsvpRequest::class.java).toCommand(guestId)
            ?: return ServerResponse.badRequest().build()

        return when (val result = guestRsvpSubmitter.submit(command)) {
            is SubmitGuestRsvpResult.Created -> ServerResponse.status(HttpStatus.CREATED).body(result.rsvp.toResponse())
            is SubmitGuestRsvpResult.Updated -> ServerResponse.ok().body(result.rsvp.toResponse())
        }
    }

    fun fetch(request: ServerRequest): ServerResponse =
        when (val result = guestRsvpGetter.get(request.requireGuestSession().guestId)) {
            is GetGuestRsvpResult.Submitted -> ServerResponse.ok().body(result.rsvp.toResponse())
            GetGuestRsvpResult.NotSubmittedYet -> ServerResponse.noContent().build()
        }
}

