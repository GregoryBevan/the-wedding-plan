package me.elgregos.theweddingplan.infrastructure.song

import me.elgregos.theweddingplan.domain.song.SongCatalog
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Component
class DeezerSongCatalog(private val deezerSearchApi: DeezerSearchApi) : SongCatalog {

    override fun search(query: String): List<SongSuggestion> =
        try {
            deezerSearchApi.search(query)
                ?.also { response ->
                    response.error?.let { throw SongCatalogUnavailableException(it.describe()) }
                }
                ?.toSuggestions()
                .orEmpty()
        } catch (e: RestClientException) {
            throw SongCatalogUnavailableException(e)
        }
}

