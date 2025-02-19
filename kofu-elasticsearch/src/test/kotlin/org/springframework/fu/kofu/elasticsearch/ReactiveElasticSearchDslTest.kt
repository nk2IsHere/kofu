package org.springframework.fu.kofu.elasticsearch

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.rest.RestStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.data.elasticsearch.client.erhlc.ReactiveElasticsearchClient
import org.springframework.fu.kofu.application
import org.testcontainers.containers.GenericContainer
import reactor.test.StepVerifier
import java.util.Collections.singletonMap


class ReactiveElasticSearchDslTest {

    @Test
    fun `enable reactive spring data elasticsearch`() {
        val es = object : GenericContainer<Nothing>("elasticsearch:7.9.3") {
            init {
                withExposedPorts(9200)
                withEnv("discovery.type", "single-node")
            }
        }
        es.start()

        val app = application {
            reactiveElasticSearch {
                hostAndPort = "localhost:${es.firstMappedPort}"
            }
        }
        with(app.run()) {
            val reactiveClient = getBean<ReactiveElasticsearchClient>()
            Assertions.assertNotNull(reactiveClient)

            val request: IndexRequest = IndexRequest("spring-data")
                    .source(singletonMap("feature", "reactive-client"))

            StepVerifier
                    .create(reactiveClient.index(request))
                    .assertNext {
                        Assertions.assertEquals(RestStatus.CREATED, it.status()) }
                    .verifyComplete()
        }
        es.stop()
    }
}
