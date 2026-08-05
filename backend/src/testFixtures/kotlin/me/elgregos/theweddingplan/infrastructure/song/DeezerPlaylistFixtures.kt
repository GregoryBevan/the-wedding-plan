package me.elgregos.theweddingplan.infrastructure.song

object DeezerPlaylistFixtures {

    // Deezer replies with a bare `true` when a track is added successfully.
    const val addTrackSuccessJson = "true"

    // A 200 carrying an `error` node, as Deezer signals an invalid/expired token.
    val invalidTokenErrorJson = """
        {"error":{"type":"OAuthException","message":"Invalid OAuth access token","code":300}}
    """.trimIndent()
}


