package me.elgregos.theweddingplan.shared

import me.elgregos.theweddingplan.domain.guest.GuestId
import java.sql.ResultSet


fun ResultSet.getUuidSet(columnLabel: String): Set<GuestId> =
    with(getArray(columnLabel)){
        try {
            (array as Array<*>).mapNotNull { GuestId.fromString(it.toString()) }.toSet()
        } finally {
            free()
        }
    }