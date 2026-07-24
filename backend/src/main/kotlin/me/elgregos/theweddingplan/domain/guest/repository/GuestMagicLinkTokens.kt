package me.elgregos.theweddingplan.domain.guest.repository

import me.elgregos.theweddingplan.domain.guest.entity.ConsumedGuestMagicLinkToken
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkAccessToken
import java.time.LocalDateTime

interface GuestMagicLinkTokens {

    fun create(guestMagicLink: GuestMagicLink)

    fun consumeIfValid(token: GuestMagicLinkAccessToken, usedAt: LocalDateTime): ConsumedGuestMagicLinkToken?
}

