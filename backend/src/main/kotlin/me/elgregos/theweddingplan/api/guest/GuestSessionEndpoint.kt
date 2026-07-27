package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.api.invitation.response.GuestSessionResponse
import me.elgregos.theweddingplan.application.guest.GuestSessionResolver
import me.elgregos.theweddingplan.application.guest.result.GuestSessionResult
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestSessionEndpoint(
    private val guestSessionResolver: GuestSessionResolver,
) {

    fun me(request: ServerRequest): ServerResponse {
        val guestSession = (request.principal().orElse(null) as? Authentication)?.principal as? GuestSession
            ?: return ServerResponse.status(HttpStatus.UNAUTHORIZED).build()

        return when (val result = guestSessionResolver.resolve(guestSession)) {
            is GuestSessionResult.Resolved -> ServerResponse.ok().body(
                GuestSessionResponse(
                    guestId = "${guestSession.guestId}",
                    invitationId = "${guestSession.invitationId}",
                    firstName = result.guest.firstName,
                    lastName = result.guest.lastName,
                )
            )

            GuestSessionResult.InvitationNotFound,
            GuestSessionResult.GuestNotInInvitation,
            -> ServerResponse.status(HttpStatus.FORBIDDEN).build()
        }
    }
}




