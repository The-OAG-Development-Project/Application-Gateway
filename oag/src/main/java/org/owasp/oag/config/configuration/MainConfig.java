package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainConfig implements ErrorValidation {

    private Map<String, LoginProvider> loginProviders;
    private Map<String, GatewayRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private String downstreamApiKey;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;
    private TraceProfile traceProfile;
    private DownstreamAuthenticationConfig downstreamAuthentication;

    public MainConfig() {
    }

    public MainConfig(Map<String, LoginProvider> loginProviders, Map<String, GatewayRoute> routes, Map<String, SecurityProfile> securityProfiles, String hostUri, String downstreamApiKey, List<String> trustedRedirectHosts, SessionBehaviour sessionBehaviour, TraceProfile traceProfile, DownstreamAuthenticationConfig downstreamAuthentication) {
        this.loginProviders = loginProviders;
        this.routes = routes;
        this.securityProfiles = securityProfiles;
        this.hostUri = hostUri;
        this.downstreamApiKey = downstreamApiKey;
        this.trustedRedirectHosts = trustedRedirectHosts;
        this.sessionBehaviour = sessionBehaviour;
        this.traceProfile = traceProfile;
        this.downstreamAuthentication = downstreamAuthentication;
    }

    public Map<String, GatewayRoute> getRoutes() {
        return routes;
    }

    private void setRoutes(Map<String, GatewayRoute> routes) {
        this.routes = routes;
    }

    public boolean isHttpsHost() {

        if (hostUri == null)
            return false;

        return getHostUri().startsWith("https://");
    }

    public String getHostUri() {
        return hostUri;
    }

    private void setHostUri(String hostUri) {
        this.hostUri = hostUri;
    }

    public Map<String, LoginProvider> getLoginProviders() {
        return loginProviders;
    }

    private void setLoginProviders(Map<String, LoginProvider> loginProviders) {
        this.loginProviders = loginProviders;
    }

    public Map<String, SecurityProfile> getSecurityProfiles() {
        return securityProfiles;
    }

    private void setSecurityProfiles(Map<String, SecurityProfile> securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    public String getDownstreamApiKey() {
        return downstreamApiKey;
    }

    private void setDownstreamApiKey(String downstreamApiKey) {
        this.downstreamApiKey = downstreamApiKey;
    }

    public List<String> getTrustedRedirectHosts() {
        return trustedRedirectHosts;
    }

    private void setTrustedRedirectHosts(List<String> trustedRedirectHosts) {
        this.trustedRedirectHosts = trustedRedirectHosts;
    }

    public SessionBehaviour getSessionBehaviour() {
        return sessionBehaviour;
    }

    private void setSessionBehaviour(SessionBehaviour sessionBehaviour) {
        this.sessionBehaviour = sessionBehaviour;
    }

    public TraceProfile getTraceProfile() {
        return traceProfile;
    }

    public void setTraceProfile(TraceProfile traceProfile) {
        this.traceProfile = traceProfile;
    }

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

        if (downstreamAuthentication == null)
            errors.add("Config: downstreamAuthentication not defined");

        if (!errors.isEmpty())
            return errors;

        // Recursive validation
        loginProviders.values().forEach(s -> errors.addAll(s.getErrors(context)));
        securityProfiles.values().forEach(s -> errors.addAll(s.getErrors(context)));
        routes.values().forEach(s -> errors.addAll(s.getErrors(context)));
        errors.addAll(sessionBehaviour.getErrors(context));
        errors.addAll(traceProfile.getErrors(context));
        errors.addAll(downstreamAuthentication.getErrors(context));

        if (!errors.isEmpty())
            return errors;

        // Cross cutting concerns
        // Check if security profile exists for each route
        routes.values().forEach(r -> {

            if (!securityProfiles.containsKey(r.getType()))
                errors.add(String.format("Security profile '%s' does not exist", r.getType()));
        });

        return errors;
    }

    public DownstreamAuthenticationConfig getDownstreamAuthentication() {
        return downstreamAuthentication;
    }

    public void setDownstreamAuthentication(DownstreamAuthenticationConfig downstreamAuthentication) {
        this.downstreamAuthentication = downstreamAuthentication;
    }
}
