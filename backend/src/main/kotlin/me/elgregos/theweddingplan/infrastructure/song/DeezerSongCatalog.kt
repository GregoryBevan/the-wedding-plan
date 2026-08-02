package me.elgregos.theweddingplan.infrastructure.song

import me.elgregos.theweddingplan.domain.song.SongCatalog
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class DeezerSongCatalog(private val deezerRestClient: RestClient) : SongCatalog {

    override fun search(query: String): List<SongSuggestion> =
        try {
            deezerRestClient.get()
                .uri { uriBuilder -> uriBuilder.path("/search").queryParam("q", query).build() }
                .retrieve()
                .body(DeezerSearchResponse::class.java)
                ?.also { response ->
                    response.error?.let {
                        throw SongCatalogUnavailableException("${it.errorCode} (code=${it.code}, message=${it.message})")
                    }
                }
                ?.toSuggestions()
                .orEmpty()
        } catch (e: RestClientException) {
            throw SongCatalogUnavailableException(e)
        }
}

