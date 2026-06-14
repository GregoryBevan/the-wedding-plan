package me.elgregos.theweddingplan

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.web.context.WebApplicationContext
import kotlin.test.BeforeTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractEndpointIntegrationTest : AbstractIntegrationTest() {
    lateinit var restTestClient: RestTestClient

    @BeforeTest
    fun setUp(context: WebApplicationContext) {
        restTestClient = RestTestClient.bindToApplicationContext(context).build()
    }
}

