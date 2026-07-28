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
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.guest.security.GUEST_SESSION_COOKIE
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import java.net.URI


@Component
class GuestAccessMagicLinkEndpoint(
    private val guestMagicLinkRequester: GuestMagicLinkRequester,
    private val guestMagicLinkVerifier: GuestMagicLinkVerifier,
    private val guestSessionTokens: GuestSessionTokens,
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
                val sessionToken = guestSessionTokens.issue(
                    GuestSession(guestId = result.guestId, invitationId = result.invitation.id)
                )

                ServerResponse.temporaryRedirect(URI.create(guestAccessProperties.guestAreaUrl))
                    .header(HttpHeaders.SET_COOKIE, guestSessionCookie(sessionToken).toString())
                    .build()
            }

            GuestMagicLinkVerificationResult.InvalidOrExpiredOrUsedToken,
            GuestMagicLinkVerificationResult.InvitationNotFound,
            GuestMagicLinkVerificationResult.GuestNotInInvitation,
                // Redirect to the guest area with an error flag, letting the SPA show a friendly,
                // recoverable "link expired or invalid" message. No session is issued.
            -> ServerResponse.status(HttpStatus.SEE_OTHER)
                .location(invalidLinkRedirectUri())
                .build()
        }
    }

    private fun invalidLinkRedirectUri(): URI =
        URI.create("${guestAccessProperties.guestAreaUrl}?linkStatus=invalid")


    private fun guestSessionCookie(token: String): ResponseCookie =
        ResponseCookie.from(GUEST_SESSION_COOKIE, token)
            .httpOnly(true)
            .secure(guestAccessProperties.sessionCookieSecure)
            .sameSite(guestAccessProperties.sessionCookieSameSite)
            .path("/")
            .maxAge(guestAccessProperties.guestSessionTtlSeconds.toLong())
            .build()
}

