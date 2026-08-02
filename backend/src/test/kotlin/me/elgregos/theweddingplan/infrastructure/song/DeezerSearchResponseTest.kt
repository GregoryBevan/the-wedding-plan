package me.elgregos.theweddingplan.infrastructure.song

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestionFixtures.laVieEnRose
import me.elgregos.theweddingplan.infrastructure.song.DeezerSearchResponseFixtures.laVieEnRoseResponse
import kotlin.test.Test

class DeezerSearchResponseTest {

    @Test
    fun `should map deezer tracks to song suggestions`() {
        assertThat(laVieEnRoseResponse.toSuggestions()).isEqualTo(listOf(laVieEnRose))
    }

    @Test
    fun `should map an empty payload to an empty list`() {
        assertThat(DeezerSearchResponse().toSuggestions()).isEmpty()
    }

    @Test
    fun `should map a blank preview to null`() {
        val track = laVieEnRoseResponse.data.orEmpty().first().copy(preview = " ")

        assertThat(DeezerSearchResponse(listOf(track)).toSuggestions().first().preview).isNull()
    }
}

