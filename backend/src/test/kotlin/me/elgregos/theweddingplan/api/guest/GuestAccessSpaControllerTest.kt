package me.elgregos.theweddingplan.api.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class GuestAccessSpaControllerTest {

    @Test
    fun `forwards the public guest routes (landing and guest-access) to the public app shell`() {
        assertThat(GuestAccessSpaController().index()).isEqualTo("forward:/public/index.html")
    }
}
