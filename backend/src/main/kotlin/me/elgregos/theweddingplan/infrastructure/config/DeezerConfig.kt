package me.elgregos.theweddingplan.infrastructure.config

import me.elgregos.theweddingplan.infrastructure.song.DeezerPlaylistApi
import me.elgregos.theweddingplan.infrastructure.song.DeezerSearchApi
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

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

    @Bean
    fun deezerSearchApi(deezerRestClient: RestClient): DeezerSearchApi =
        deezerProxyFactory(deezerRestClient).createClient(DeezerSearchApi::class.java)

    @Bean
    fun deezerPlaylistApi(deezerRestClient: RestClient): DeezerPlaylistApi =
        deezerProxyFactory(deezerRestClient).createClient(DeezerPlaylistApi::class.java)

    private fun deezerProxyFactory(deezerRestClient: RestClient): HttpServiceProxyFactory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(deezerRestClient)).build()
}



