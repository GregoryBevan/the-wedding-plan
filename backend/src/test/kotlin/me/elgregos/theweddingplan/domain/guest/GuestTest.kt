package me.elgregos.theweddingplan.domain.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.guest
import kotlin.test.Test

class GuestTest {

    @Test
    fun `should create guest`() {
        val guest = guest()
        
        assertThat(guest.id).isNotNull()
        assertThat(guest.version).isEqualTo(1L)
        assertThat(guest.creationDate).isNotNull()
        assertThat(guest.updateDate).isNotNull()
        assertThat(guest.firstName).isEqualTo("John")
        assertThat(guest.lastName).isEqualTo("Doe")
        assertThat(guest.email).isEqualTo("john.doe@example.com")
    }
}
