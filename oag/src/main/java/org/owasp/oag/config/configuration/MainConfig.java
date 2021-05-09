package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
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

public class MainConfig implements ErrorValidation {

    private static final Logger log = LoggerFactory.getLogger(MainConfig.class);

    private Map<String, LoginProvider> loginProviders;
    private Map<String, GatewayRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;
    private TraceProfile traceProfile;

    private URL url;

    public MainConfig() {
    }

    public MainConfig(Map<String, LoginProvider> loginProviders, Map<String, GatewayRoute> routes, Map<String, SecurityProfile> securityProfiles, String hostUri, List<String> trustedRedirectHosts, SessionBehaviour sessionBehaviour, TraceProfile traceProfile) {
        this.loginProviders = loginProviders;
        this.routes = routes;
        this.securityProfiles = securityProfiles;
        this.hostUri = hostUri;
        this.trustedRedirectHosts = trustedRedirectHosts;
        this.sessionBehaviour = sessionBehaviour;
        this.traceProfile = traceProfile;

        try {
            if(hostUri != null)
                this.url = new URL(hostUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid hostUri", e);
        }
    }

    public Map<String, GatewayRoute> getRoutes() {
        return routes;
    }

    private void setRoutes(Map<String, GatewayRoute> routes) {
        this.routes = routes;
    }

    public boolean isHttpsHost() {

        if(this.url == null) {
            try {
                this.url = new URL(this.hostUri);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid hostUri", e);
            }
        }
        return "https".equals(url.getProtocol());
    }

    public Map<String, SecurityProfile> getUsedSecurityProfiles() {

        var profiles = getSecurityProfiles();
        var routes = getRoutes();

        Set<String> usedProfileNames = routes.values().stream().map(route -> route.getType()).collect(Collectors.toSet());
        var usedProfiles = profiles.entrySet()
                .stream().filter(entry -> usedProfileNames.contains(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        return usedProfiles;
    }

    public String getHostUri() {
        return hostUri;
    }

    public String getHostname(){

        return url.getHost();
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

        if (!errors.isEmpty())
            return errors;

        try{
            URL parsed = new URL(hostUri);
            var protocol = parsed.getProtocol();

            if("http".equals(protocol)){
                log.warn("Protocol is http which should only be used for development purposes");
            }else if(! "https".equals(protocol)){
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
}
