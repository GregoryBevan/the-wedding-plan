package me.elgregos.theweddingplan.api.song.response

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.api.song.response.SongSuggestionResponseFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestionFixtures
import kotlin.test.Test

class SongSuggestionResponseTest {

    @Test
    fun `should map a suggestion to a response`() {
        assertThat(SongSuggestionFixtures.laVieEnRose.toResponse()).isEqualTo(laVieEnRose)
    }
}

