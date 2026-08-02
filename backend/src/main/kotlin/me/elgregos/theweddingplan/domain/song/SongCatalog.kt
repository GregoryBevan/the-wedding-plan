package me.elgregos.theweddingplan.domain.song

import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion

/**
 * Outbound port to a music catalog (e.g. Deezer) used to suggest songs for a guest's choice.
 * Implementations translate provider failures into [SongCatalogUnavailableException].
 */
interface SongCatalog {

    fun search(query: String): List<SongSuggestion>
}

