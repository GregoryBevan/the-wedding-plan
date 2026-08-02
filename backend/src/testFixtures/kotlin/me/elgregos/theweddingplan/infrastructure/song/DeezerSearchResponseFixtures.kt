package me.elgregos.theweddingplan.infrastructure.song

object DeezerSearchResponseFixtures {

    val laVieEnRoseResponse = DeezerSearchResponse(
        data = listOf(
            DeezerTrack(
                id = 3135556L,
                title = "La Vie en rose",
                link = "https://www.deezer.com/track/3135556",
                preview = "https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3",
                artist = DeezerArtist(name = "Édith Piaf"),
            ),
        ),
    )

    // Raw Deezer payload including fields we don't consume (e.g. `type`) to prove the mapping ignores unknowns.
    val laVieEnRoseJson = """
        {"data":[{"id":3135556,"title":"La Vie en rose","link":"https://www.deezer.com/track/3135556","preview":"https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3","artist":{"id":1093,"name":"Édith Piaf"},"type":"track"}]}
    """.trimIndent()

    // Deezer signals failures (rate limiting, quota, invalid query, ...) with an HTTP 200 carrying an `error` node.
    val quotaExceededErrorJson = """
        {"error":{"type":"Exception","message":"Quota limit exceeded","code":4}}
    """.trimIndent()
}

