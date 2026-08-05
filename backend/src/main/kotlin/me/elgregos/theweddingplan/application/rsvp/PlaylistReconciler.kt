package me.elgregos.theweddingplan.application.rsvp

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PlaylistReconciler(
    private val guestRsvps: GuestRsvps,
    private val playlistSynchronizer: PlaylistSynchronizer,
) {

    @Scheduled(cron = $$"${app.playlist.reconcile-cron:0 0 3 * * *}")
    fun reconcile() {
        val pending = guestRsvps.findGuestsWithUnsynchronizedSong()
        if (pending.isEmpty()) return

        logger.info { "Reconciling ${pending.size} pending song(s) with the wedding playlist" }
        pending.forEach { playlistSynchronizer.sync(it.guestId, it.song) }
    }
}

