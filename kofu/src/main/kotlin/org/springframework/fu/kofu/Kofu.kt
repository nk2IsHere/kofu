package org.springframework.fu.kofu

import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.support.GenericApplicationContext

/**
 * Declare a command-line [application][ApplicationDsl] (no server) that allows to configure a Spring Boot
 * application using Kofu DSL and functional bean registration.
 *
 * @sample org.springframework.fu.kofu.samples.webFluxApplicationDsl
 * @param dsl The `application { }` DSL
 * @see ApplicationDsl
 * @see webApplication
 * @see reactiveWebApplication
 * @author Sebastien Deleuze
 */
fun application(dsl: ApplicationDsl.() -> Unit) = object : KofuApplication(ApplicationDsl(dsl)) {
    override fun createContext(): ConfigurableApplicationContext {
        return GenericApplicationContext()
    }
}

/**
 * Define a configuration that can be imported in an application or used in tests.
 * @see ConfigurationDsl.enable
 * @sample org.springframework.fu.kofu.samples.applicationDslWithConfiguration
 */
fun configuration(dsl: ConfigurationDsl.() -> Unit) = ConfigurationDsl(dsl)
