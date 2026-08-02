package me.elgregos.theweddingplan.application.song

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.elgregos.theweddingplan.application.song.result.SongSearchResult
import me.elgregos.theweddingplan.domain.song.SongCatalog
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestionFixtures.laVieEnRose
import kotlin.test.BeforeTest
import kotlin.test.Test

class SongSearcherTest {

    private lateinit var songCatalog: SongCatalog
    private lateinit var songSearcher: SongSearcher

    @BeforeTest
    fun setUp() {
        songCatalog = mockk()
        songSearcher = SongSearcher(songCatalog)
    }

    @Test
    fun `should return empty suggestions for a blank query without hitting the catalog`() {
        assertThat(songSearcher.search("   ")).isEqualTo(SongSearchResult.Suggestions(emptyList()))

        verify(exactly = 0) { songCatalog.search(any()) }
    }

    @Test
    fun `should return the catalog suggestions for a trimmed query`() {
        every { songCatalog.search("piaf") } returns listOf(laVieEnRose)

        assertThat(songSearcher.search("  piaf  "))
            .isEqualTo(SongSearchResult.Suggestions(listOf(laVieEnRose)))
    }

    @Test
    fun `should return unavailable when the catalog is unavailable`() {
        every { songCatalog.search("piaf") } throws SongCatalogUnavailableException(RuntimeException("down"))

        assertThat(songSearcher.search("piaf")).isEqualTo(SongSearchResult.Unavailable)
    }
}



