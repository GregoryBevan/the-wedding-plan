package me.elgregos.theweddingplan.infrastructure.guest.service

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

/**
 * Declarative HTTP client for Brevo's transactional email API. Backed by the `brevoRestClient`
 * (see `BrevoConfig`), which carries the base URL and the `api-key` header. A non-2xx response
 * surfaces as a `RestClientException` for the caller to handle. Used instead of SMTP because the
 * hosting platform (Render) blocks outbound SMTP ports.
 */
interface BrevoApi {

    @PostExchange("/v3/smtp/email")
    fun sendTransactionalEmail(@RequestBody request: BrevoSendEmailRequest)
}

data class BrevoSendEmailRequest(
    val sender: BrevoContact,
    val to: List<BrevoContact>,
    val subject: String,
    val htmlContent: String,
    val textContent: String,
)

data class BrevoContact(
    val email: String,
    val name: String? = null,
)

