package me.elgregos.theweddingplan.infrastructure.guest.service

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import org.springframework.stereotype.Component

@Component
class NoopGuestMagicLinkSender : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) = Unit
}