package me.elgregos.theweddingplan.infrastructure.song

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.Test

class DeezerErrorCodeTest {

    @ParameterizedTest
    @EnumSource(DeezerErrorCode::class)
    fun `should map each code back to its enum constant`(errorCode: DeezerErrorCode) {
        assertThat(DeezerErrorCode.fromCode(errorCode.code)).isEqualTo(errorCode)
    }

    @Test
    fun `should map an undocumented code to unknown`() {
        assertThat(DeezerErrorCode.fromCode(42)).isEqualTo(DeezerErrorCode.UNKNOWN)
    }
}


