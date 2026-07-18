package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.api.auth.AuthRateLimiter
import me.elgregos.theweddingplan.api.common.clientAddress
import me.elgregos.theweddingplan.api.common.guestIdPathParam
import me.elgregos.theweddingplan.api.common.invitationAccessTokenPathParam
import me.elgregos.theweddingplan.api.invitation.response.GuestAccessMagicLinkResponse
import me.elgregos.theweddingplan.api.invitation.response.MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE
import me.elgregos.theweddingplan.application.guest.GuestMagicLinkRequester
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestAccessMagicLinkEndpoint(
    private val guestMagicLinkRequester: GuestMagicLinkRequester,
    private val authRateLimiter: AuthRateLimiter,
) {

    fun requestMagicLink(request: ServerRequest): ServerResponse {
        val decision = authRateLimiter.check("guest-magic-link:${request.clientAddress()}")

        if (!decision.allowed) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", decision.retryAfterSeconds.toString())
                .build()
        }

        val token = request.invitationAccessTokenPathParam() ?: return ServerResponse.badRequest().build()
        val guestId = request.guestIdPathParam("guestId") ?: return ServerResponse.badRequest().build()
        val command = RequestGuestMagicLinkCommand(invitationAccessToken = token, guestId = guestId)

        guestMagicLinkRequester.request(command)

        return ServerResponse.accepted().body(GuestAccessMagicLinkResponse(MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE))
    }
}
