package me.elgregos.theweddingplan.application.song.result

import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion

sealed interface SongSearchResult {
    data class Suggestions(val suggestions: List<SongSuggestion>) : SongSearchResult
    data object Unavailable : SongSearchResult
}

