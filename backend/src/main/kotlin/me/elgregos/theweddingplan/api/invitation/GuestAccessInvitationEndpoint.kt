package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.api.common.invitationAccessTokenPathParam
import me.elgregos.theweddingplan.api.invitation.response.toPublicResponse
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestAccessInvitationEndpoint(
    private val invitationTokenResolver: InvitationTokenResolver,
) {

    fun resolveByAccessToken(request: ServerRequest): ServerResponse =
        request.invitationAccessTokenPathParam()
            ?.let { token ->
                invitationTokenResolver.resolve(token)
                    ?.toPublicResponse()
                    ?.let(ServerResponse.ok()::body)
                    ?: ServerResponse.notFound().build()
            }
            ?: ServerResponse.badRequest().build()
}




