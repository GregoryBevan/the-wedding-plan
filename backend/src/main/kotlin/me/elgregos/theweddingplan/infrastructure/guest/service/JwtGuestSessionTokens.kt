package me.elgregos.theweddingplan.infrastructure.guest.service

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import javax.crypto.spec.SecretKeySpec

private const val INVITATION_ID_CLAIM = "invitationId"

@Service
class JwtGuestSessionTokens(
    private val guestAccessProperties: GuestAccessProperties,
) : GuestSessionTokens {

    private val secretKey = SecretKeySpec(guestAccessProperties.jwtSecret.toByteArray(), "HmacSHA256")
    private val encoder = NimbusJwtEncoder(ImmutableSecret<SecurityContext>(secretKey))
    private val decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()

    override fun issue(session: GuestSession): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .subject(session.guestId.toString())
            .claim(INVITATION_ID_CLAIM, session.invitationId.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(guestAccessProperties.guestSessionTtlSeconds.toLong()))
            .build()

        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).tokenValue
    }

    override fun verify(token: String): GuestSession? {
        val jwt = runCatching { decoder.decode(token) }.getOrNull() ?: return null

        val guestId = jwt.subject?.let(GuestId::fromStringOrNull) ?: return null
        val invitationId = jwt.getClaimAsString(INVITATION_ID_CLAIM)?.let(InvitationId::fromStringOrNull) ?: return null

        return GuestSession(guestId = guestId, invitationId = invitationId)
    }
}


