package org.springframework.fu.kofu

import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.context.ConfigurableApplicationContext

/**
 * Declare a Reactive-based web [application][ApplicationDsl] that allows to configure a Spring Boot
 * application using Kofu DSL and functional bean registration.
 *
 * @sample org.springframework.fu.kofu.samples.webFluxApplicationDsl
 * @param dsl The `application { }` DSL
 * @see ApplicationDsl
 * @author Sebastien Deleuze
 */
fun reactiveWebApplication(dsl: ApplicationDsl.() -> Unit)
    = object: KofuApplication(ApplicationDsl(dsl)) {
    override fun createContext(): ConfigurableApplicationContext {
        return ReactiveWebServerApplicationContext()
    }
}
