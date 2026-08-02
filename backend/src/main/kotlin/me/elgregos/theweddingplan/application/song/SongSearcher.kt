package me.elgregos.theweddingplan.application.song

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.application.song.result.SongSearchResult
import me.elgregos.theweddingplan.domain.song.SongCatalog
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SongSearcher(private val songCatalog: SongCatalog) {

    fun search(query: String): SongSearchResult {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return SongSearchResult.Suggestions(emptyList())

        return try {
            SongSearchResult.Suggestions(songCatalog.search(trimmed))
        } catch (e: SongCatalogUnavailableException) {
            logger.warn(e) { "Song catalog unavailable for query '$trimmed'" }
            SongSearchResult.Unavailable
        }
    }
}




