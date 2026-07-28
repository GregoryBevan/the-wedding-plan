package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.api.common.statusQueryParam
import me.elgregos.theweddingplan.api.common.availabilityQueryParam
import me.elgregos.theweddingplan.api.common.guestIdPathParam
import me.elgregos.theweddingplan.api.common.intQueryParam
import me.elgregos.theweddingplan.api.guest.request.AddGuestRequest
import me.elgregos.theweddingplan.api.guest.request.UpdateGuestRequest
import me.elgregos.theweddingplan.api.guest.response.toResponse
import me.elgregos.theweddingplan.application.guest.*
import me.elgregos.theweddingplan.application.guest.result.ArchiveGuestResult
import me.elgregos.theweddingplan.application.guest.result.RestoreGuestResult
import me.elgregos.theweddingplan.application.guest.result.UpdateGuestResult
import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestListCriteria
import me.elgregos.theweddingplan.infrastructure.config.GuestProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestEndpoint(
    private val guestAdder: GuestAdder,
    private val guestLister: GuestLister,
    private val guestGetter: GuestGetter,
    private val guestArchiver: GuestArchiver,
    private val guestRestorer: GuestRestorer,
    private val guestUpdater: GuestUpdater,
    private val guestProperties: GuestProperties,
) {

    fun listGuests(request: ServerRequest): ServerResponse {
        val page = request.intQueryParam("page", 0) ?: return ServerResponse.badRequest().build()
        val size = request.intQueryParam("size", 20) ?: return ServerResponse.badRequest().build()
        val status = request.statusQueryParam() ?: return ServerResponse.badRequest().build()
        val availability = request.availabilityQueryParam() ?: return ServerResponse.badRequest().build()
        val search = request.param("search").orElse(null)?.trim()?.takeIf(String::isNotEmpty)

        return if (page < 0 || size <= 0) {
            ServerResponse.badRequest().build()
        } else {
            ServerResponse.ok().body(
                    guestLister.list(
                        GuestListCriteria(page = page, size = size, status = status, availability = availability, search = search)
                    ).toResponse()
            )
        }
    }

    fun addGuest(request: ServerRequest): ServerResponse =
        request.body(AddGuestRequest::class.java)
            .toCommand(guestProperties.defaultLanguage)
            .let(guestAdder::add)
            .let(Guest::toResponse)
            .let { ServerResponse.status(HttpStatus.CREATED).body(it) }

    fun getGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()

        return guestGetter.get(id)
            ?.let(Guest::toResponse)
            ?.let(ServerResponse.ok()::body)
            ?: ServerResponse.notFound().build()
    }

    fun updateGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()
        val payload = request.body(UpdateGuestRequest::class.java)

        return with(guestUpdater.update(payload.toCommand(id, guestProperties.defaultLanguage))) {
            when (this) {
                is UpdateGuestResult.Updated -> ServerResponse.ok().body(guest.toResponse())
                is UpdateGuestResult.NotFound -> ServerResponse.notFound().build()
                is UpdateGuestResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT).build()
            }
        }
    }

    fun archiveGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()

        return when (val result = guestArchiver.archive(id)) {
            is ArchiveGuestResult.Archived -> ServerResponse.ok().body(result.guest.toResponse())
            is ArchiveGuestResult.NotFound -> ServerResponse.notFound().build()
            is ArchiveGuestResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT).build()
        }
    }

    fun restoreGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()

        return when (val result = guestRestorer.restore(id)) {
            is RestoreGuestResult.Restored -> ServerResponse.ok().body(result.guest.toResponse())
            is RestoreGuestResult.NotFound -> ServerResponse.notFound().build()
            is RestoreGuestResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT).build()
        }
    }
}
