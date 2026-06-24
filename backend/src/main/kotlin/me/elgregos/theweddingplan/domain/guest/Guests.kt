package me.elgregos.theweddingplan.domain.guest

interface Guests {

    fun add(guest: Guest): Guest
    fun update(guest: Guest, expectedVersion: Long): Guest?
    fun findById(id: GuestId): Guest?
    fun list(): List<Guest>
    fun list(page: Int, size: Int): GuestPage
}
