package me.elgregos.theweddingplan.domain.guest.service

import me.elgregos.theweddingplan.domain.guest.entity.GuestSession

interface GuestSessionTokens {

    fun issue(session: GuestSession): String

    fun verify(token: String): GuestSession?
}

