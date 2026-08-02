package me.elgregos.theweddingplan.api.song

import me.elgregos.theweddingplan.api.song.response.toResponse
import me.elgregos.theweddingplan.application.song.SongSearcher
import me.elgregos.theweddingplan.application.song.result.SongSearchResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import kotlin.jvm.optionals.getOrNull

@Component
class SongSearchEndpoint(private val songSearcher: SongSearcher) {

    fun search(request: ServerRequest): ServerResponse =
        when (val result = songSearcher.search(request.param("q").getOrNull() ?: "")) {
            is SongSearchResult.Suggestions -> ServerResponse.ok().body(result.suggestions.map { it.toResponse() })
            SongSearchResult.Unavailable -> ServerResponse.status(HttpStatus.BAD_GATEWAY).build()
        }
}


