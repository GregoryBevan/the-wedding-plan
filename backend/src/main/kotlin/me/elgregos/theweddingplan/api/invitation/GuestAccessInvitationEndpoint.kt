package me.elgregos.theweddingplan.api.invitation

import me.elgregos.theweddingplan.api.common.invitationAccessTokenPathParam
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.invitation.Invitation
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestAccessInvitationEndpoint(private val invitationTokenResolver: InvitationTokenResolver) {

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

data class PublicInvitationResponse(
    val label: String,
    val description: String,
    val guests: List<PublicInvitationGuestResponse>,
    val guestCount: Int,
)

data class PublicInvitationGuestResponse(
    val firstName: String,
    val lastName: String,
)

internal fun Invitation.toPublicResponse() = PublicInvitationResponse(
    label = label,
    description = description,
    guests = guests
        .sortedBy { it.id.toString() }
        .map {
            PublicInvitationGuestResponse(
                firstName = it.firstName,
                lastName = it.lastName,
            )
        },
    guestCount = guests.size,
)




