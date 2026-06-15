package me.elgregos.theweddingplan

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractEndpointIntegrationTest : AbstractIntegrationTest() {
    @LocalServerPort
    protected var port: Int = 0

    protected val restTestClient: RestTestClient
        get() = RestTestClient.bindToServer().baseUrl("http://localhost:$port").build()
}

