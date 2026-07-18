package me.elgregos.theweddingplan.domain.guest.service

import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink

interface GuestMagicLinkSender {

    fun send(guestMagicLink: GuestMagicLink)
}