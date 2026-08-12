package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertions.isNotNull
import me.elgregos.theweddingplan.infrastructure.guest.service.BrevoApi
import org.springframework.web.client.RestClient
import kotlin.test.BeforeTest
import kotlin.test.Test

class BrevoConfigTest {

    private lateinit var brevoConfig: BrevoConfig
    private lateinit var brevoProperties: BrevoProperties

    @BeforeTest
    fun setUp() {
        brevoConfig = BrevoConfig()
        brevoProperties = BrevoProperties(apiKey = "test-api-key")
    }

    @Test
    fun `should build brevo rest client`() {
        val restClient = brevoConfig.brevoRestClient(RestClient.builder(), brevoProperties)

        assertThat(restClient).isNotNull()
    }

    @Test
    fun `should build brevo api client`() {
        val restClient = brevoConfig.brevoRestClient(RestClient.builder(), brevoProperties)

        val brevoApi: BrevoApi = brevoConfig.brevoApi(restClient)

        assertThat(brevoApi).isNotNull()
    }
}

