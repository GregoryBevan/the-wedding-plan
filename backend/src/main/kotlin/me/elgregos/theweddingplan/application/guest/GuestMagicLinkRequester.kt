package me.elgregos.theweddingplan.application.guest

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import me.elgregos.theweddingplan.application.guest.result.RequestGuestMagicLinkResult
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GuestMagicLinkRequester(
    private val invitationTokenResolver: InvitationTokenResolver,
    private val guestMagicLinkSender: GuestMagicLinkSender,
) {

    fun request(command: RequestGuestMagicLinkCommand): RequestGuestMagicLinkResult {
        val invitation = invitationTokenResolver.resolve(command.invitationAccessToken)
            ?: return RequestGuestMagicLinkResult.InvitationNotFound

        val guest = invitation.guests.firstOrNull { it.id == command.guestId }
            ?: return RequestGuestMagicLinkResult.GuestNotFound

        return runCatching {
            guestMagicLinkSender.send(
                GuestMagicLink(
                    invitationId = invitation.id,
                    invitationAccessToken = invitation.accessToken,
                    guestId = guest.id,
                    guestFirstName = guest.firstName,
                    guestEmail = guest.email,
                )
            )
            RequestGuestMagicLinkResult.Sent
        }.getOrElse {
            logger.warn(it) { "Magic-link delivery failed (invitationId=${invitation.id}, guestId=${guest.id})" }
            RequestGuestMagicLinkResult.DeliveryFailed
        }
    }

}
