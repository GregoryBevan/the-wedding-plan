package me.elgregos.theweddingplan.domain.guest

interface Guests {

    fun add(guest: Guest): Guest
    fun update(guest: Guest, expectedVersion: Long): Guest?
    fun findById(id: GuestId): Guest?
    fun findArchivedById(id: GuestId): Guest?
    fun restore(guest: Guest, expectedVersion: Long): Guest?
    fun list(criteria: GuestListCriteria): GuestPage
}
