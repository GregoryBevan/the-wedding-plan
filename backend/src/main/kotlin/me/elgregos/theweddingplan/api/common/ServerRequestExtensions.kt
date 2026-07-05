package me.elgregos.theweddingplan.api.common

import me.elgregos.theweddingplan.domain.guest.GuestStatus
import me.elgregos.theweddingplan.domain.guest.GuestAvailability
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import org.springframework.web.servlet.function.ServerRequest

private const val STATUS_PARAM_NAME = "status"
private const val AVAILABILITY_PARAM_NAME = "availability"

internal fun ServerRequest.intQueryParam(name: String, default: Int): Int? =
    queryParamOrNull(name)?.toIntOrNull() ?: if (param(name).isEmpty) default else null

internal fun ServerRequest.statusQueryParam(): GuestStatus? =
    queryParamOrNull(STATUS_PARAM_NAME)?.toGuestStatus() ?: if (param(STATUS_PARAM_NAME).isEmpty) GuestStatus.ACTIVE else null

internal fun ServerRequest.availabilityQueryParam(): GuestAvailability? =
    queryParamOrNull(AVAILABILITY_PARAM_NAME)?.toGuestAvailability()
        ?: if (param(AVAILABILITY_PARAM_NAME).isEmpty) GuestAvailability.ALL else null

internal fun ServerRequest.guestIdPathParam(): GuestId? =
    runCatching {
        pathVariable("id")
            .takeIf(String::isNotEmpty)
            ?.let { GuestId.fromString(it) }
    }.getOrNull()

internal fun ServerRequest.invitationIdPathParam() =
    runCatching {
        pathVariable("id")
            .takeIf(String::isNotEmpty)
            ?.let { InvitationId.fromString(it) }
    }.getOrNull()

private fun ServerRequest.queryParamOrNull(name: String) =
    param(name).orElse(null)

private fun String.toGuestStatus() =
    GuestStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

private fun String.toGuestAvailability() =
    GuestAvailability.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }


