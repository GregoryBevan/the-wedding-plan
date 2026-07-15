package me.elgregos.theweddingplan.application.guest

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.application.guest.UpdateGuestCommandFixtures.johnDoeUpdated as johnDoeUpdatedCommand
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoeArchived
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.Guests
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestUpdaterTest {

    private lateinit var guests: Guests
    private lateinit var guestUpdater: GuestUpdater

    @BeforeTest
    fun setUp() {
        guests = mockk()
        guestUpdater = GuestUpdater(guests)
    }

    @Test
    fun `should update existing guest`() {
        val guest = johnDoe
        val command = johnDoeUpdatedCommand.copy(version = guest.version)
        every { guests.findById(guest.id) } returns guest
        every { guests.update(any(), guest.version) } answers { firstArg() }

        val updatedGuest = (guestUpdater.update(command) as UpdateGuestResult.Updated).guest

        assertThat(updatedGuest).all {
            prop(Guest::id).isEqualTo(guest.id)
            prop(Guest::version).isEqualTo(guest.version + 1)
            prop(Guest::creationDate).isEqualTo(guest.creationDate)
            prop(Guest::firstName).isEqualTo(command.firstName)
            prop(Guest::lastName).isEqualTo(command.lastName)
            prop(Guest::email).isEqualTo(command.email)
        }
    }

    @Test
    fun `should return not found when updating non existing guest`() {
        val guestId = johnDoe.id
        val command = johnDoeUpdatedCommand.copy(version = 1L)
        every { guests.findById(guestId) } returns null

        assertThat(guestUpdater.update(command)).isEqualTo(UpdateGuestResult.NotFound)
    }

    @Test
    fun `should return version conflict when expected version does not match current guest`() {
        val guest = johnDoe
        val command = johnDoeUpdatedCommand.copy(version = guest.version + 1)
        every { guests.findById(guest.id) } returns guest

        assertThat(guestUpdater.update(command)).isEqualTo(UpdateGuestResult.VersionConflict)
    }

    @Test
    fun `should return version conflict when repository detects stale version`() {
        val guest = johnDoe
        val command = johnDoeUpdatedCommand.copy(version = guest.version)
        every { guests.findById(guest.id) } returns guest
        every { guests.update(any(), guest.version) } returns null

        assertThat(guestUpdater.update(command)).isEqualTo(UpdateGuestResult.VersionConflict)
    }

    @Test
    fun `should return not found when updating archived guest`() {
        val command = johnDoeUpdatedCommand.copy(version = johnDoeArchived.version)
        every { guests.findById(johnDoeArchived.id) } returns null

        assertThat(guestUpdater.update(command)).isEqualTo(UpdateGuestResult.NotFound)
    }
}

