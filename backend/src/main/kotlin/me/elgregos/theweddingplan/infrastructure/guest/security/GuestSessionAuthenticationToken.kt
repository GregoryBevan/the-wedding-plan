package me.elgregos.theweddingplan.infrastructure.guest.security

import me.elgregos.theweddingplan.domain.guest.entity.GuestSession
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class GuestSessionAuthenticationToken private constructor(
    private val guestSession: GuestSession,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getPrincipal(): GuestSession = guestSession

    override fun getCredentials(): Any? = null

    companion object {
        private val GUEST_AUTHORITIES = listOf(SimpleGrantedAuthority("ROLE_GUEST"))

        fun authenticated(guestSession: GuestSession) =
            GuestSessionAuthenticationToken(guestSession, GUEST_AUTHORITIES)
    }
}

