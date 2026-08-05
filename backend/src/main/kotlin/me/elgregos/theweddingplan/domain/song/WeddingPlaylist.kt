package me.elgregos.theweddingplan.domain.song

/**
 * Outbound port to the shared wedding playlist hosted on a music provider (e.g. Deezer).
 *
 * Implementations translate provider failures into [WeddingPlaylistUnavailableException] so
 * the caller can isolate a sync failure from the guest's RSVP.
 */
interface WeddingPlaylist {

    /**
     * Adds the track to the shared playlist. De-duplication is the caller's responsibility, so this
     * is a plain "append" to the provider's playlist.
     */
    fun addTrack(deezerId: Long)

    /**
     * Removes the track from the shared playlist. Deciding whether the track is still wanted by
     * another guest is the caller's responsibility, so this is a plain "remove" from the provider's
     * playlist.
     */
    fun removeTrack(deezerId: Long)
}



