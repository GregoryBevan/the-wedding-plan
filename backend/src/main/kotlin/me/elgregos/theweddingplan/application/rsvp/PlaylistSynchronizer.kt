package me.elgregos.theweddingplan.application.rsvp

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.SongChoice
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import me.elgregos.theweddingplan.domain.song.WeddingPlaylist
import me.elgregos.theweddingplan.domain.song.WeddingPlaylistUnavailableException
import me.elgregos.theweddingplan.infrastructure.shared.warnWithDetails
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}

/**
 * Pushes a guest's chosen song to the shared wedding playlist and records that it is now synced, and
 * removes a track once no guest chooses it anymore.
 *
 * De-duplication is derived from the RSVP answers: if the track is already on the playlist (another
 * guest synced it) it is not added again, only flagged as synced for this guest. The song is flagged
 * as synced only once the provider confirms it, so a failed sync stays pending for the reconciler to
 * retry.
 *
 * The check-then-act (is-on-playlist then add, is-still-chosen then remove) is not atomic on its own,
 * so concurrent submissions and reconciliations for the same track could otherwise add duplicates or
 * let a removal race an addition. Each track is therefore guarded by its own lock, serialising add and
 * remove for the same [deezerId][SongChoice.deezerId] while leaving unrelated tracks parallel. The app
 * runs as a single instance (see render.yaml), so an in-process lock is sufficient; a multi-instance
 * deployment would need a shared lock (e.g. a Postgres advisory lock) instead.
 *
 * Best-effort: any provider failure is logged and swallowed so it can never fail the guest's RSVP.
 */
@Service
class PlaylistSynchronizer(
    private val guestRsvps: GuestRsvps,
    private val weddingPlaylist: WeddingPlaylist,
) {

    private val trackLocks = ConcurrentHashMap<Long, ReentrantLock>()

    /**
     * Fire-and-forget entry point used on the submission path: runs [sync] on a task-executor thread
     * so the Deezer call never adds latency to (nor can fail) the guest's RSVP response. The endpoint
     * itself stays synchronous.
     */
    @Async
    fun syncAsync(guestId: GuestId, song: SongChoice) = sync(guestId, song)

    fun sync(guestId: GuestId, song: SongChoice) {
        if (song.synchronized) return

        lockFor(song.deezerId).withLock {
            if (!guestRsvps.isSongOnPlaylist(song.deezerId)) {
                try {
                    weddingPlaylist.addTrack(song.deezerId)
                } catch (e: WeddingPlaylistUnavailableException) {
                    logger.warnWithDetails(e, "Could not add a track to the wedding playlist") { "Could not add track ${song.deezerId} ('${song.title}') to the wedding playlist" }
                    return
                }
            }

            guestRsvps.markSongSynchronized(guestId)
        }
    }

    /**
     * Fire-and-forget entry point used on the submission path: runs [unsync] on a task-executor thread
     * so the Deezer call never adds latency to (nor can fail) the guest's RSVP response.
     */
    @Async
    fun unsyncAsync(deezerId: Long) = unsync(deezerId)

    fun unsync(deezerId: Long) {
        lockFor(deezerId).withLock {
            if (guestRsvps.isSongChosenByAnyGuest(deezerId)) return

            try {
                weddingPlaylist.removeTrack(deezerId)
            } catch (e: WeddingPlaylistUnavailableException) {
                logger.warnWithDetails(e, "Could not remove a track from the wedding playlist") { "Could not remove track $deezerId from the wedding playlist" }
            }
        }
    }

    private fun lockFor(deezerId: Long): ReentrantLock =
        trackLocks.computeIfAbsent(deezerId) { ReentrantLock() }
}



