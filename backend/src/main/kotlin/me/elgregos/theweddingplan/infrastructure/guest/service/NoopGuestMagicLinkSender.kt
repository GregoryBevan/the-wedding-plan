package me.elgregos.theweddingplan.infrastructure.guest.service

import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "noop")
class NoopGuestMagicLinkSender : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) = Unit
}