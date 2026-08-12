package me.elgregos.theweddingplan.api.rsvp

import me.elgregos.theweddingplan.api.common.bindOrBadRequest
import me.elgregos.theweddingplan.api.common.requireGuestSession
import me.elgregos.theweddingplan.api.rsvp.request.SubmitRsvpRequest
import me.elgregos.theweddingplan.api.rsvp.response.toResponse
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpGetter
import me.elgregos.theweddingplan.application.rsvp.GuestRsvpSubmitter
import me.elgregos.theweddingplan.application.rsvp.result.GetGuestRsvpResult
import me.elgregos.theweddingplan.application.rsvp.result.SubmitGuestRsvpResult
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestRsvpEndpoint(
    private val guestRsvpSubmitter: GuestRsvpSubmitter,
    private val guestRsvpGetter: GuestRsvpGetter,
    private val validator: Validator,
) {

    fun submit(request: ServerRequest): ServerResponse {
        val guestId = request.requireGuestSession().guestId

        return request.bindOrBadRequest<SubmitRsvpRequest>(validator) { payload ->
            payload.toCommand(guestId)
                ?.let { command ->
                    when (val result = guestRsvpSubmitter.submit(command)) {
                        is SubmitGuestRsvpResult.Created -> ServerResponse.status(HttpStatus.CREATED).body(result.rsvp.toResponse())
                        is SubmitGuestRsvpResult.Updated -> ServerResponse.ok().body(result.rsvp.toResponse())
                    }
                } ?: ServerResponse.badRequest().build()
        }
    }

    fun fetch(request: ServerRequest): ServerResponse =
        when (val result = guestRsvpGetter.get(request.requireGuestSession().guestId)) {
            is GetGuestRsvpResult.Submitted -> ServerResponse.ok().body(result.rsvp.toResponse())
            GetGuestRsvpResult.NotSubmittedYet -> ServerResponse.noContent().build()
        }
}
