package org.springframework.security.config.annotation.web.configuration;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@link ApplicationContextInitializer} adapter for {@link HttpSecurityConfiguration}.
 */
public class HttpSecurityInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
    private static final String BEAN_NAME_PREFIX = "org.springframework.security.config.annotation.web.configuration.HttpSecurityConfiguration.";
    static final String HTTPSECURITY_BEAN_NAME = BEAN_NAME_PREFIX + "httpSecurity";

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsPasswordService userDetailsPasswordService;

    private AuthenticationManager authenticationManager;

    public HttpSecurityInitializer(
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        UserDetailsPasswordService userDetailsPasswordService
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsPasswordService = userDetailsPasswordService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(GenericApplicationContext context) {
        HttpSecurityConfiguration configuration = new HttpSecurityConfiguration();
        configuration.setApplicationContext(context);


        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        authenticationConfiguration.setApplicationContext(context);

        configuration.setAuthenticationConfiguration(authenticationConfiguration);

        if(authenticationManager == null) {
            // Build authenticationManager, otherwise HttpSecurityConfiguration will throw NPE
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setUserDetailsPasswordService(userDetailsPasswordService);

            authProvider.setPasswordEncoder(Objects.requireNonNullElseGet(
                passwordEncoder,
                PasswordEncoderFactories::createDelegatingPasswordEncoder
            ));

            authenticationManager = new ProviderManager(authProvider);
        }

        context.registerBean(
            AuthenticationManagerBuilder.class,
            () -> new AuthenticationManagerBuilder(context.getBean(ObjectPostProcessor.class))
                .parentAuthenticationManager(authenticationManager)
        );

        Supplier<HttpSecurity> httpSecuritySupplier = () -> {
            configuration.setObjectPostProcessor(context.getBean(ObjectPostProcessor.class));

            try {
                return configuration.httpSecurity();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        context.registerBean(
            HTTPSECURITY_BEAN_NAME,
            HttpSecurity.class,
            httpSecuritySupplier,
            bd -> {
                bd.setScope("prototype");
                bd.setAutowireCandidate(true);
            }
        );
    }
}
