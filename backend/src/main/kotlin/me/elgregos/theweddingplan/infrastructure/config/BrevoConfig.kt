package me.elgregos.theweddingplan.infrastructure.config

import me.elgregos.theweddingplan.infrastructure.guest.service.BrevoApi
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

/**
 * Wires the Brevo transactional-email HTTP client. Only loaded when `app.mail.provider=brevo`, so
 * non-Brevo environments (local SMTP/Mailpit, tests) never require a Brevo API key.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "brevo")
@EnableConfigurationProperties(BrevoProperties::class)
class BrevoConfig {

    @Bean
    fun brevoRestClient(builder: RestClient.Builder, properties: BrevoProperties): RestClient =
        builder
            .baseUrl(properties.baseUrl)
            .defaultHeader("api-key", properties.apiKey)
            .defaultHeader("accept", "application/json")
            .build()

    @Bean
    fun brevoApi(brevoRestClient: RestClient): BrevoApi =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(brevoRestClient))
            .build()
            .createClient(BrevoApi::class.java)
}

