package org.springframework.fu.kofu

import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ConfigurableApplicationContext


/**
 * Declare a Servlet-based web [application][ApplicationDsl] that allows to configure a Spring Boot
 * application using Kofu DSL and functional bean registration.
 *
 * @sample org.springframework.fu.kofu.samples.webFluxApplicationDsl
 * @param dsl The `application { }` DSL
 * @see ApplicationDsl
 * @author Sebastien Deleuze
 */
fun webApplication(dsl: ApplicationDsl.() -> Unit)
    = object: KofuApplication(ApplicationDsl(dsl)) {
    override fun createContext(): ConfigurableApplicationContext {
        return ServletWebServerApplicationContext()
    }
}
