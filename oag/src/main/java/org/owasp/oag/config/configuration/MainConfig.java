package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main configuration class for the OWASP Application Gateway.
 * This class holds all configuration parameters and provides validation
 * of the configuration settings against business rules.
 */
public class MainConfig implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(MainConfig.class);

    private Map<String, LoginProvider> loginProviders;
    private Map<String, GatewayRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;
    private TraceProfile traceProfile;
    private KeyManagementProfile keyManagementProfile;

    private URL url;

    /**
     * Default constructor for the main configuration.
     */
    public MainConfig() {
    }

    /**
     * Creates a fully initialized main configuration with all required parameters.
     *
     * @param loginProviders The map of available login providers
     * @param routes The map of gateway routes
     * @param securityProfiles The map of security profiles
     * @param hostUri The host URI of the application gateway
     * @param trustedRedirectHosts List of trusted redirect hosts
     * @param sessionBehaviour The session behavior configuration
     * @param traceProfile The tracing profile configuration
     * @param keyManagementProfile The key management profile configuration
     */
    public MainConfig(Map<String, LoginProvider> loginProviders, Map<String, GatewayRoute> routes, Map<String, SecurityProfile> securityProfiles, String hostUri, List<String> trustedRedirectHosts, SessionBehaviour sessionBehaviour, TraceProfile traceProfile, KeyManagementProfile keyManagementProfile) {
        this.loginProviders = loginProviders;
        this.routes = routes;
        this.securityProfiles = securityProfiles;
        this.hostUri = hostUri;
        this.trustedRedirectHosts = trustedRedirectHosts;
        this.sessionBehaviour = sessionBehaviour;
        this.traceProfile = traceProfile;
        this.keyManagementProfile = keyManagementProfile;

        try {
            if (hostUri != null)
                this.url = new URL(hostUri);
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Invalid hostUri", e);
        }
    }

    /**
     * Gets the configured gateway routes.
     *
     * @return Map of route names to route configurations
     */
    public Map<String, GatewayRoute> getRoutes() {
        return routes;
    }

    /**
     * Sets the gateway routes.
     *
     * @param routes Map of route names to route configurations
     */
    private void setRoutes(Map<String, GatewayRoute> routes) {
        this.routes = routes;
    }

    /**
     * Checks if the host is using HTTPS protocol.
     *
     * @return true if the host uses HTTPS, false otherwise
     * @throws ConfigurationException if the host URI is invalid
     */
    public boolean isHttpsHost() {

        if (this.url == null) {
            try {
                this.url = new URL(this.hostUri);
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Invalid hostUri", e);
            }
        }
        return "https".equals(url.getProtocol());
    }

    /**
     * Gets the security profiles that are actually used by the configured routes.
     *
     * @return Map of security profile names to their configurations
     */
    public Map<String, SecurityProfile> getUsedSecurityProfiles() {

        var profiles = getSecurityProfiles();
        var routes = getRoutes();

        Set<String> usedProfileNames = routes.values().stream().map(GatewayRoute::getType).collect(Collectors.toSet());

        return profiles.entrySet()
                .stream()
                .filter(entry -> usedProfileNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Gets the host URI of the application gateway.
     *
     * @return The host URI
     */
    public String getHostUri() {
        return hostUri;
    }

    /**
     * Gets the hostname part of the host URI.
     *
     * @return The hostname
     */
    public String getHostname() {

        return url.getHost();
    }

    /**
     * Sets the host URI of the application gateway.
     *
     * @param hostUri The host URI
     */
    private void setHostUri(String hostUri) {
        this.hostUri = hostUri;
    }

    /**
     * Gets the configured login providers.
     *
     * @return Map of provider names to provider configurations
     */
    public Map<String, LoginProvider> getLoginProviders() {
        return loginProviders;
    }

    /**
     * Sets the login providers configuration.
     *
     * @param loginProviders Map of provider names to provider configurations
     */
    private void setLoginProviders(Map<String, LoginProvider> loginProviders) {
        this.loginProviders = loginProviders;
    }

    /**
     * Gets all configured security profiles.
     *
     * @return Map of security profile names to their configurations
     */
    public Map<String, SecurityProfile> getSecurityProfiles() {
        return securityProfiles;
    }

    /**
     * Sets the security profiles configuration.
     *
     * @param securityProfiles Map of security profile names to their configurations
     */
    private void setSecurityProfiles(Map<String, SecurityProfile> securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    /**
     * Gets the list of trusted redirect hosts.
     *
     * @return List of trusted redirect hosts
     */
    public List<String> getTrustedRedirectHosts() {
        return trustedRedirectHosts;
    }

    /**
     * Sets the list of trusted redirect hosts.
     *
     * @param trustedRedirectHosts List of trusted redirect hosts
     */
    private void setTrustedRedirectHosts(List<String> trustedRedirectHosts) {
        this.trustedRedirectHosts = trustedRedirectHosts;
    }

    /**
     * Gets the session behavior configuration.
     *
     * @return The session behavior configuration
     */
    public SessionBehaviour getSessionBehaviour() {
        return sessionBehaviour;
    }

    /**
     * Sets the session behavior configuration.
     *
     * @param sessionBehaviour The session behavior configuration
     */
    private void setSessionBehaviour(SessionBehaviour sessionBehaviour) {
        this.sessionBehaviour = sessionBehaviour;
    }

    /**
     * Gets the trace profile configuration.
     *
     * @return The trace profile configuration
     */
    public TraceProfile getTraceProfile() {
        return traceProfile;
    }

    /**
     * Sets the trace profile configuration.
     *
     * @param traceProfile The trace profile configuration
     */
    public void setTraceProfile(TraceProfile traceProfile) {
        this.traceProfile = traceProfile;
    }

    /**
     * Gets the key management profile configuration.
     *
     * @return The key management profile configuration
     */
    public KeyManagementProfile getKeyManagementProfile() {
        return keyManagementProfile;
    }

    /**
     * Sets the key management profile configuration.
     *
     * @param keyManagementProfile The key management profile configuration
     */
    public void setKeyManagementProfile(KeyManagementProfile keyManagementProfile) {
        this.keyManagementProfile = keyManagementProfile;
    }

    /**
     * Validates the configuration and returns any errors found.
     *
     * @param context The application context
     * @return A list of validation error messages, empty if no errors are found
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (loginProviders == null)
            errors.add("Config: loginProviders not defined");

        if (routes == null)
            errors.add("Config: routes not defined");

        if (hostUri == null)
            errors.add("Config: hostUri not defined");

        if (securityProfiles == null)
            errors.add("Config: securityProfiles not defined");

        if (trustedRedirectHosts == null)
            errors.add("Config: trustedRedirectHosts not defined");

        if (sessionBehaviour == null)
            errors.add("Config: sessionBehaviour not defined");

        if (traceProfile == null)
            errors.add("Config: traceProfile not defined");

        if (keyManagementProfile == null)
            errors.add("Config: keyManagementProfile not defined");

        if (!errors.isEmpty())
            return errors;

        try {
            URL parsed = new URL(hostUri);
            var protocol = parsed.getProtocol();

            if ("http".equals(protocol)) {
                log.warn("Protocol is http which should only be used for development purposes");
            } else if (!"https".equals(protocol)) {
                errors.add("Invalid protocol for hostUri: " + protocol);
            }
        } catch (MalformedURLException e) {
            errors.add("Config: hostUri is not a valid URL");
        }

        if (!errors.isEmpty())
            return errors;

        // Recursive validation
        loginProviders.values().forEach(s -> errors.addAll(s.getErrors(context)));
        securityProfiles.values().forEach(s -> errors.addAll(s.getErrors(context)));
        routes.values().forEach(s -> errors.addAll(s.getErrors(context)));
        errors.addAll(sessionBehaviour.getErrors(context));
        errors.addAll(traceProfile.getErrors(context));
        errors.addAll(keyManagementProfile.getErrors(context));

        if (!errors.isEmpty())
            return errors;

        // Cross-cutting concerns
        // Check if security profile exists for each route
        routes.values().forEach(r -> {

            if (!securityProfiles.containsKey(r.getType()))
                errors.add(String.format("Security profile '%s' does not exist", r.getType()));
        });

        return errors;
    }
}
