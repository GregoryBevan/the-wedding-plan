package me.elgregos.theweddingplan.infrastructure.song

/**
 * Documented Deezer API error codes, returned in the `error.code` field of an HTTP 200 payload.
 * See https://developers.deezer.com/api/errors. [UNKNOWN] covers any undocumented/absent code.
 */
enum class DeezerErrorCode(val code: Int?) {
    QUOTA(4),
    ITEMS_LIMIT_EXCEEDED(100),
    PERMISSION(200),
    TOKEN_INVALID(300),
    PARAMETER(500),
    PARAMETER_MISSING(501),
    QUERY_INVALID(600),
    SERVICE_BUSY(700),
    DATA_NOT_FOUND(800),
    INDIVIDUAL_ACCOUNT_NOT_ALLOWED(901),
    UNKNOWN(null);

    companion object {
        fun fromCode(code: Int?): DeezerErrorCode =
            entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

