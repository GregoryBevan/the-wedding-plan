package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(DeezerProperties::class)
class DeezerConfig {

    @Bean
    fun deezerRestClient(builder: RestClient.Builder, properties: DeezerProperties): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(properties.connectTimeout)
            setReadTimeout(properties.readTimeout)
        }

        return builder
            .baseUrl(properties.baseUrl)
            .requestFactory(requestFactory)
            .build()
    }
}



