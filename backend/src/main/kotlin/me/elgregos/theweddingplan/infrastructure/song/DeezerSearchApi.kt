package me.elgregos.theweddingplan.infrastructure.song

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

/**
 * Declarative HTTP client for Deezer's public search API. Backed by the shared `deezerRestClient`
 * (see `DeezerConfig`); non-2xx responses surface as `RestClientException` for the adapter to translate.
 */
interface DeezerSearchApi {

    @GetExchange("/search")
    fun search(@RequestParam("q") query: String): DeezerSearchResponse?
}

