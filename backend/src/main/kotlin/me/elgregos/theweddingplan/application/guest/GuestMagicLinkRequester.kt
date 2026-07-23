package me.elgregos.theweddingplan.application.guest

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import me.elgregos.theweddingplan.application.guest.result.RequestGuestMagicLinkResult
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.repository.GuestMagicLinkTokens
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.domain.shared.Dates.nowUtcMillis
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GuestMagicLinkRequester(
    private val invitationTokenResolver: InvitationTokenResolver,
    private val guestMagicLinkTokens: GuestMagicLinkTokens,
    private val guestMagicLinkSender: GuestMagicLinkSender,
    private val guestAccessProperties: GuestAccessProperties,
) {

    fun request(command: RequestGuestMagicLinkCommand): RequestGuestMagicLinkResult {
        val invitation = invitationTokenResolver.resolve(command.invitationAccessToken)
            ?: return RequestGuestMagicLinkResult.InvitationNotFound

        val guest = invitation.guests.firstOrNull { it.id == command.guestId }
            ?: return RequestGuestMagicLinkResult.GuestNotFound

        return runCatching {
            val guestMagicLink = GuestMagicLink(
                invitationId = invitation.id,
                guestId = guest.id,
                expiresAt = nowUtcMillis().plusSeconds(guestAccessProperties.magicLinkTtlSeconds),
            ).also(guestMagicLinkTokens::create)

            guestMagicLinkSender.send(guestMagicLink, guest)
            RequestGuestMagicLinkResult.Sent
        }.getOrElse {
            logger.warn(it) { "Magic-link delivery failed (invitationId=${invitation.id}, guestId=${guest.id})" }
            RequestGuestMagicLinkResult.DeliveryFailed
        }
    }

}
