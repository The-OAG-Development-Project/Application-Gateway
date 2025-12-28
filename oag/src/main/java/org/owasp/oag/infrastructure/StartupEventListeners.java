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

/**
 * Handles various application startup events.
 * This component listens for Spring application events and performs
 * validation and logging tasks when the application starts up.
 */
@Component
public class StartupEventListeners {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(OWASPApplicationGatewayApplication.class);

    /**
     * The main configuration of the application.
     * Contains information about routes, security profiles, and other configuration settings.
     */
    @Autowired
    MainConfig config;

    /**
     * The Spring application context.
     * Used to access beans and validate configuration.
     */
    @Autowired
    ApplicationContext context;

    /**
     * Validates the application configuration after the Spring context is initialized.
     * This method is triggered by the ContextRefreshedEvent and checks for any
     * configuration errors, throwing an exception if any are found.
     *
     * @throws ConfigurationException if the configuration contains errors
     */
    @EventListener(ContextRefreshedEvent.class)
    public void validateConfigurationAfterContextInitialized() {

        var configErrors = config.getErrors(context);
        if (!configErrors.isEmpty()) {
            String message = "Configuration file contains errors: " + configErrors;
            throw new ConfigurationException(message);
        }

    }

    /**
     * Logs information about the application configuration after startup.
     * This method is triggered by the ApplicationStartedEvent and logs details
     * about login providers, gateway routes, and other configuration information.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void logInfo() {

        // Log login providers
        var providers = config.getLoginProviders().entrySet().stream()
                .map(e -> String.format("%s: type=%s", e.getKey(), e.getValue().getType()))
                .collect(Collectors.joining(", "));

        log.info("Login Providers: [{}]", providers);

        // Log current routes
        config.getRoutes().forEach((name, route) ->{

            log.info("GatewayRoute {} {} => {} type={} allowAnonymous={} rewrite: {} => {}",
                    String.format("%-25s", name + ":"),
                    String.format("%-25s", route.getPath()), String.format("%-41s", route.getUrl() + ","),
                    String.format("%-21s", route.getType() + ","), String.format("%-6s", route.isAllowAnonymous()+","),
                    route.getRewrite().getRegex(), route.getRewrite().getReplacement());

        });


        log.info("OWASP Application Gateway started with {} routes", config.getRoutes().size());
    }
}
