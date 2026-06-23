package me.elgregos.theweddingplan.domain.guest

interface Guests {

    fun add(guest: Guest): Guest
    fun update(guest: Guest): Guest
    fun list(): List<Guest>
    fun list(page: Int, size: Int): GuestPage
}
