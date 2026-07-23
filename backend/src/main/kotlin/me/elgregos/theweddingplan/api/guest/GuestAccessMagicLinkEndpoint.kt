package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.api.auth.AuthRateLimiter
import me.elgregos.theweddingplan.api.common.clientAddress
import me.elgregos.theweddingplan.api.common.guestIdPathParam
import me.elgregos.theweddingplan.api.common.invitationAccessTokenPathParam
import me.elgregos.theweddingplan.api.common.magicLinkTokenPathParam
import me.elgregos.theweddingplan.api.invitation.response.GuestAccessMagicLinkResponse
import me.elgregos.theweddingplan.api.invitation.response.MAGIC_LINK_REQUEST_ACCEPTED_MESSAGE
import me.elgregos.theweddingplan.application.guest.GuestMagicLinkVerifier
import me.elgregos.theweddingplan.application.guest.GuestMagicLinkRequester
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import me.elgregos.theweddingplan.application.guest.result.GuestMagicLinkVerificationResult
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import java.net.URI

private const val GUEST_SESSION_GUEST_ID = "guestAccessGuestId"
private const val GUEST_SESSION_INVITATION_ID = "guestAccessInvitationId"

@Component
class GuestAccessMagicLinkEndpoint(
    private val guestMagicLinkRequester: GuestMagicLinkRequester,
    private val guestMagicLinkVerifier: GuestMagicLinkVerifier,
    private val authRateLimiter: AuthRateLimiter,
    private val guestAccessProperties: GuestAccessProperties,
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

    fun verifyMagicLink(request: ServerRequest): ServerResponse {
        val token = request.magicLinkTokenPathParam() ?: return ServerResponse.notFound().build()

        return when (val result = guestMagicLinkVerifier.verify(token)) {
            is GuestMagicLinkVerificationResult.Verified -> {
                request.servletRequest().getSession(true).apply {
                    maxInactiveInterval = guestAccessProperties.guestSessionTtlSeconds
                    setAttribute(GUEST_SESSION_GUEST_ID, result.guestId.toString())
                    setAttribute(GUEST_SESSION_INVITATION_ID, result.invitation.id.toString())
                }

                ServerResponse.temporaryRedirect(URI.create(guestAccessProperties.guestAreaUrl)).build()
            }

            GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken,
            GuestMagicLinkVerificationResult.InvitationNotFound,
            GuestMagicLinkVerificationResult.GuestNotInInvitation,
            -> ServerResponse.notFound().build()
        }
    }
}

