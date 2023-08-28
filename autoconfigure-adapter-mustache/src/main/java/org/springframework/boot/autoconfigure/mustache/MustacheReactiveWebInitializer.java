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

package org.springframework.boot.autoconfigure.mustache;

import com.samskivert.mustache.Mustache;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * {@link ApplicationContextInitializer} adapter for {@link MustacheReactiveWebConfiguration}.
 */
public class MustacheReactiveWebInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private final MustacheProperties properties;

    public MustacheReactiveWebInitializer(MustacheProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        MustacheReactiveWebConfiguration configuration = new MustacheReactiveWebConfiguration();

        context.registerBean(
            ViewResolver.class,
            () -> configuration.mustacheViewResolver(
                context.getBean(Mustache.Compiler.class),
                this.properties
            )
        );
    }
}