package org.springframework.fu.kofu.webflux

/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.jupiter.api.Test
import org.springframework.fu.kofu.localServerPort
import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.templating.thymeleaf
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

/**
 * @author Sebastien Deleuze
 */
class ThymeleafDslTests {

    @Test
    fun `Create and request a Thymeleaf view`() {
        val app = reactiveWebApplication {
            webFlux {
                port = 0
                thymeleaf()
                router {
                    GET("/view") { ok().render("template", mapOf("name" to "world")) }
                }
            }
        }
        with(app.run()) {
            val client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:$localServerPort").build()
            client.get().uri("/view").exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<String>()
                .isEqualTo("<p>Hello world!</p>")
            close()
        }
    }
}
