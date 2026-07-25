package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkEmailTemplateTest {

    private lateinit var guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate

    @BeforeTest
    fun setUp() {
        guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate()
    }

    @Test
    fun `should render a user friendly text body`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val textBody = guestMagicLinkEmailTemplate.textBody(janeDoe.firstName, magicLinkUrl)

        assertThat(textBody).all {
            contains("Bonjour ${janeDoe.firstName}")
            contains("lien sécurisé")
            contains("heureux de vous inviter")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render html body with cta and fallback link`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(janeDoe.firstName, magicLinkUrl)

        assertThat(htmlBody).all {
            contains("<html lang=\"fr\">")
            contains("Thecla & Grégory")
            contains("Bonjour ${janeDoe.firstName}")
            contains("Accéder à mon invitation")
            contains(magicLinkUrl)
        }

    }
}

