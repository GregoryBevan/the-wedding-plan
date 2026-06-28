package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.application.guest.AddGuestCommand
import me.elgregos.theweddingplan.application.guest.GuestGetter
import me.elgregos.theweddingplan.application.guest.GuestLister
import me.elgregos.theweddingplan.application.guest.UpdateGuestResult
import me.elgregos.theweddingplan.application.guest.GuestUpdater
import me.elgregos.theweddingplan.application.guest.UpdateGuestCommand
import me.elgregos.theweddingplan.domain.guest.GuestActiveFilter
import me.elgregos.theweddingplan.domain.guest.GuestListCriteria
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.GuestPage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestEndpoint(
    private val guestAdder: GuestAdder,
    private val guestLister: GuestLister,
    private val guestGetter: GuestGetter,
    private val guestUpdater: GuestUpdater,
) {

    fun listGuests(request: ServerRequest): ServerResponse {
        val page = request.intQueryParam("page", default = 0) ?: return ServerResponse.badRequest().build()
        val size = request.intQueryParam("size", default = 20) ?: return ServerResponse.badRequest().build()

        return if (page < 0 || size <= 0) {
            ServerResponse.badRequest().build()
        } else {
            ServerResponse.ok().body(guestLister.list(GuestListCriteria(page = page, size = size, activeFilter = GuestActiveFilter.ACTIVE)).toResponse())
        }
    }

    fun addGuest(request: ServerRequest): ServerResponse =
        request.body(AddGuestRequest::class.java)
            .toCommand()
            .let(guestAdder::add)
            .toResponse()
            .let { ServerResponse.status(HttpStatus.CREATED).body(it) }

    fun getGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()

        return guestGetter.get(id)
            ?.toResponse()
            ?.let(ServerResponse.ok()::body)
            ?: ServerResponse.notFound().build()
    }

    fun updateGuest(request: ServerRequest): ServerResponse {
        val id = request.guestIdPathParam() ?: return ServerResponse.badRequest().build()
        val payload = request.body(UpdateGuestRequest::class.java)

        return with(guestUpdater.update(id, payload.toCommand())) {
            when (this) {
                is UpdateGuestResult.Updated -> ServerResponse.ok().body(this.guest.toResponse())
                is UpdateGuestResult.NotFound -> ServerResponse.notFound().build()
                is UpdateGuestResult.VersionConflict -> ServerResponse.status(HttpStatus.CONFLICT).build()
            }
        }
    }
}

private fun ServerRequest.intQueryParam(name: String, default: Int): Int? =
    param(name).orElse(null)
        ?.trim()
        .let { value ->
            when {
                value.isNullOrEmpty() -> default
                else -> value.toIntOrNull()
            }
        }

private fun ServerRequest.guestIdPathParam() =
    pathVariable("id")
        .trim()
        .takeIf(String::isNotEmpty)
        ?.let { runCatching { GuestId.fromString(it) }.getOrNull() }

data class GuestResponse(
    val id: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

data class GuestPageResponse(
    val items: List<GuestResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

data class AddGuestRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
)

data class UpdateGuestRequest(
    val version: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
)

internal fun Guest.toResponse() =
    GuestResponse(id.toString(), version, creationDate.toString(), updateDate.toString(), firstName, lastName, email)

internal fun AddGuestRequest.toCommand() =
    AddGuestCommand(firstName = firstName, lastName = lastName, email = email)

internal fun UpdateGuestRequest.toCommand() =
    UpdateGuestCommand(version = version, firstName = firstName, lastName = lastName, email = email)

internal fun GuestPage.toResponse() =
    GuestPageResponse(
        items = items.map(Guest::toResponse),
        page = page,
        size = size,
        totalItems = totalItems,
        totalPages = totalPages,
    )

