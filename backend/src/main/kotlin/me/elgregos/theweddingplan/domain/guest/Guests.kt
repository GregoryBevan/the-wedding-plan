package me.elgregos.theweddingplan.domain.guest

import me.elgregos.theweddingplan.domain.guest.Guest

interface Guests {
    fun add(guest: Guest): Guest
}
