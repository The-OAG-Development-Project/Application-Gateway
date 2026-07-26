package org.owasp.oag.integration.testInfrastructure;

import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.ConfigLoader;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.persistentmap.InMemoryPersistentMap;
import org.owasp.oag.services.blacklist.LocalPersistentBlacklist;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.utils.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Configuration class for integration test. Overwrites the default beans.
 */
@TestConfiguration
@Import(OWASPApplicationGatewayApplication.class)
public class IntegrationTestConfig {

    /**
     * Global clock source for time-based operations.
     */
    @Autowired
    GlobalClockSource clockSource;

    /**
     * Spring application context.
     */
    @Autowired
    ApplicationContext context;

    /**
     * Uses a in-memory blacklist for sessions.
     *
     * @return the SessionBlacklist implementation.
     */
    @Primary
    @Bean(destroyMethod = "close")
    public SessionBlacklist sessionBlacklist() {
        return new LocalPersistentBlacklist(clockSource, new InMemoryPersistentMap<>());
    }

    /**
     * Port number of the wiremock port that is randomly selected.
     * Is 0 if no wiremock is used in the test.
     */
    @Value("${wiremock.server.port:0}")
    protected int wiremockPort;

    /**
     * Overwrites the url of the gateway routes with the url of the wiremock instance.
     * This allows tests to route requests to a local wiremock server instead of external endpoints.
     * 
     * @param loader Config file loader
     * @return Overwritten configuration file
     * @throws RuntimeException if the configuration cannot be loaded
     */
    @Bean
    public MainConfig mainConfig(ConfigLoader loader){

        try {

            MainConfig config = loader.loadConfiguration();

            // Override port of routes and endpoints with random wiremock port
            if(wiremockPort != 0){

                // Routes
                for(var routeEntry : config.getRoutes().entrySet()){

                    var route = routeEntry.getValue();
                    var newUrl = UrlUtils.replacePortOfUrlString(route.getUrl(), wiremockPort);
                    var newRoute = new GatewayRoute(route.getPath(), newUrl, route.getType(), route.isAllowAnonymous(), route.getRewrite());

                    routeEntry.setValue(newRoute);
                }

                // Login provider endpoint
                config.getLoginProviders().values().forEach(provider -> {

                    var providerSettings = provider.getWith();
                    replacePortOfUrlInMap("tokenEndpoint", providerSettings);
                    replacePortOfUrlInMap("authEndpoint", providerSettings);
                });
            }

            return config;

        } catch (Exception e) {
            throw new RuntimeException("Could not load OAG configuration", e);
        }
    }

    /**
     * Helper method to replace the port in a URL stored in a map.
     * 
     * @param key The key in the map containing the URL to modify
     * @param map The map containing the URL
     * @throws RuntimeException if the URL is malformed
     */
    private void replacePortOfUrlInMap(String key, Map<String, Object> map){

        map.computeIfPresent(key, (key2, url) -> {
            try {
                return UrlUtils.replacePortOfUrlString((String)url, wiremockPort);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid url");
            }
        });

    }
}
