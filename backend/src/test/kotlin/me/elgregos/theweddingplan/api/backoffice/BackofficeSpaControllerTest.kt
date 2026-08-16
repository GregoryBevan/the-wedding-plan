package me.elgregos.theweddingplan.api.backoffice

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class BackofficeSpaControllerTest {

    @Test
    fun `forwards the backoffice routes to the backoffice app shell`() {
        assertThat(BackofficeSpaController().index()).isEqualTo("forward:/backoffice/index.html")
    }
}

