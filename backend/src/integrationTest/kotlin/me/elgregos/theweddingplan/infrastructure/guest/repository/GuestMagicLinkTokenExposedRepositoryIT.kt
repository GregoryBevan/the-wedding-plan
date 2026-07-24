package me.elgregos.theweddingplan.infrastructure.guest.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.entity.ConsumedGuestMagicLinkToken
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.repository.GuestMagicLinkTokens
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.shared.Dates.nowUtcMillis
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class GuestMagicLinkTokenExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var guestMagicLinkTokens: GuestMagicLinkTokens

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should persist magic-link token`() {
        val token = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))

        guestMagicLinkTokens.create(token)

        assertThat(tokenCount(token.token.value)).isEqualTo(1)
    }

    @Test
    fun `should keep only one active token per guest when creating a new one`() {
        val firstToken = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(firstToken)

        val secondToken = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(secondToken)

        val consumedFirstToken = guestMagicLinkTokens.consumeIfValid(firstToken.token, nowUtcMillis())
        val consumedSecondToken = guestMagicLinkTokens.consumeIfValid(secondToken.token, nowUtcMillis())

        assertThat(consumedFirstToken).isNull()
        assertThat(consumedSecondToken).isEqualTo(
            ConsumedGuestMagicLinkToken(
                invitationId = bridesMaidInvitation.id,
                guestId = janeDoe.id,
            )
        )
        assertThat(activeTokenCountForGuest(janeDoe.id)).isEqualTo(0)
    }

    @Test
    fun `should mark previous token as used when creating a new token for same guest`() {
        val firstToken = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(firstToken)

        val secondToken = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(secondToken)

        assertThat(usedTokenCount(firstToken.token.value)).isEqualTo(1)
        assertThat(activeTokenCountForGuest(janeDoe.id)).isEqualTo(1)
    }

    @Test
    fun `should not invalidate active tokens of other guests`() {
        val johnToken = newMagicLinkToken(guestId = johnDoe.id, expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(johnToken)

        val janeToken = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(janeToken)

        val consumedJohnToken = guestMagicLinkTokens.consumeIfValid(johnToken.token, nowUtcMillis())

        assertThat(consumedJohnToken).isEqualTo(
            ConsumedGuestMagicLinkToken(
                invitationId = bridesMaidInvitation.id,
                guestId = johnDoe.id,
            )
        )
    }

    @Test
    fun `should consume valid token and return invitation and guest`() {
        val token = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(token)

        val consumed = guestMagicLinkTokens.consumeIfValid(token.token, nowUtcMillis())

        assertThat(consumed).isEqualTo(
            ConsumedGuestMagicLinkToken(
                invitationId = bridesMaidInvitation.id,
                guestId = janeDoe.id,
            )
        )
    }

    @Test
    fun `should mark token as used when consumed`() {
        val token = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(token)

        guestMagicLinkTokens.consumeIfValid(token.token, nowUtcMillis())

        assertThat(usedTokenCount(token.token.value)).isEqualTo(1)
    }

    @Test
    fun `should return null when token is already used`() {
        val token = newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15))
        guestMagicLinkTokens.create(token)
        guestMagicLinkTokens.consumeIfValid(token.token, nowUtcMillis())

        val consumedAgain = guestMagicLinkTokens.consumeIfValid(token.token, nowUtcMillis())

        assertThat(consumedAgain).isNull()
    }

    @Test
    fun `should return null when token is expired`() {
        val token = newMagicLinkToken(expiresAt = nowUtcMillis().minusSeconds(1))
        guestMagicLinkTokens.create(token)

        val consumed = guestMagicLinkTokens.consumeIfValid(token.token, nowUtcMillis())

        assertThat(consumed).isNull()
    }

    @Test
    fun `should keep one active token and avoid failures under concurrent creation for same guest`() {
        val errors = ConcurrentLinkedQueue<Throwable>()
        val startGate = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)

        repeat(20) {
            executor.submit {
                startGate.await()
                runCatching {
                    guestMagicLinkTokens.create(newMagicLinkToken(expiresAt = nowUtcMillis().plusMinutes(15)))
                }.onFailure(errors::add)
            }
        }

        startGate.countDown()
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        assertThat(errors.isEmpty() && activeTokenCountForGuest(janeDoe.id) == 1).isEqualTo(true)
    }

    private fun newMagicLinkToken(guestId: GuestId = janeDoe.id, expiresAt: LocalDateTime) = GuestMagicLink(
        invitationId = bridesMaidInvitation.id,
        guestId = guestId,
        expiresAt = expiresAt,
    )

    private fun tokenCount(token: String) =
        jdbcTemplate.queryForObject(
            "select count(*) from guest_magic_link_token where token = ?",
            Int::class.java,
            token,
        ) ?: 0

    private fun usedTokenCount(token: String) =
        jdbcTemplate.queryForObject(
            "select count(*) from guest_magic_link_token where token = ? and used_at is not null",
            Int::class.java,
            token,
        ) ?: 0

    private fun activeTokenCountForGuest(guestId: GuestId) =
        jdbcTemplate.queryForObject(
            "select count(*) from guest_magic_link_token where guest_id = ? and used_at is null",
            Int::class.java,
            guestId.value.toJavaUuid(),
        ) ?: 0
}



