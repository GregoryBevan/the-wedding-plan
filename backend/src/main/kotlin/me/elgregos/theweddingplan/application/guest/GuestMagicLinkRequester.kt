package me.elgregos.theweddingplan.application.guest

import me.elgregos.theweddingplan.application.guest.command.RequestGuestMagicLinkCommand
import me.elgregos.theweddingplan.application.invitation.InvitationTokenResolver
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import org.springframework.stereotype.Service

@Service
class GuestMagicLinkRequester(
    private val invitationTokenResolver: InvitationTokenResolver,
    private val guestMagicLinkSender: GuestMagicLinkSender,
) {

    fun request(command: RequestGuestMagicLinkCommand) {
        val invitation = invitationTokenResolver.resolve(command.invitationAccessToken) ?: return
        val guest = invitation.guests.firstOrNull { it.id == command.guestId } ?: return

        guestMagicLinkSender.send(
            GuestMagicLink(
                invitationId = invitation.id,
                invitationAccessToken = invitation.accessToken,
                guestId = guest.id,
                guestEmail = guest.email,
            )
        )
    }
}

