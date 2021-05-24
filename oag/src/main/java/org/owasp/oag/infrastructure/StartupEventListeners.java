package org.owasp.oag.infrastructure;

import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class StartupEventListeners {

    private static final Logger log = LoggerFactory.getLogger(OWASPApplicationGatewayApplication.class);

    @Autowired
    MainConfig config;

    @Autowired
    ApplicationContext context;

    @EventListener(ContextRefreshedEvent.class)
    public void validateConfigurationAfterContextInitialized() {

        var configErrors = config.getErrors(context);
        if (!configErrors.isEmpty()) {
            String message = "Configuration file contains errors: " + configErrors;
            throw new ConfigurationException(message);
        }

    }

    @EventListener(ApplicationStartedEvent.class)
    public void logInfo() {

        // Log login providers
        var providers = config.getLoginProviders().entrySet().stream()
                .map(e -> String.format("%s: type=%s", e.getKey(), e.getValue().getType()))
                .collect(Collectors.joining(", "));

        log.info("Login Providers: [{}]", providers);
        log.info("OWASP Application Gateway started with {} routes", config.getRoutes().size());
    }
}
