package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
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

        val textBody = guestMagicLinkEmailTemplate.textBody(bridesMaidToJane.guestFirstName, magicLinkUrl)

        assertThat(textBody).contains("Bonjour ${bridesMaidToJane.guestFirstName}")
        assertThat(textBody).contains("lien sécurisé")
        assertThat(textBody).contains("heureux de vous inviter")
        assertThat(textBody).contains(magicLinkUrl)
    }

    @Test
    fun `should render html body with cta and fallback link`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(bridesMaidToJane.guestFirstName, magicLinkUrl)

        assertThat(htmlBody).contains("<html lang=\"fr\">")
        assertThat(htmlBody).contains("Thecla & Grégory")
        assertThat(htmlBody).contains("Bonjour ${bridesMaidToJane.guestFirstName}")
        assertThat(htmlBody).contains("Accéder à mon invitation")
        assertThat(htmlBody).contains(magicLinkUrl)
    }
}

