package ch.gianlucafrei.nellygateway.config.configuration;

import ch.gianlucafrei.nellygateway.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NellyConfig implements ErrorValidation {

    private Map<String, LoginProvider> loginProviders;
    private Map<String, NellyRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private String nellyApiKey;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;
    private TraceProfile traceProfile;

    public NellyConfig() {
    }

    public NellyConfig(Map<String, LoginProvider> loginProviders, Map<String, NellyRoute> routes, Map<String, SecurityProfile> securityProfiles, String hostUri, String nellyApiKey, List<String> trustedRedirectHosts, SessionBehaviour sessionBehaviour, TraceProfile traceProfile) {
        this.loginProviders = loginProviders;
        this.routes = routes;
        this.securityProfiles = securityProfiles;
        this.hostUri = hostUri;
        this.nellyApiKey = nellyApiKey;
        this.trustedRedirectHosts = trustedRedirectHosts;
        this.sessionBehaviour = sessionBehaviour;
        this.traceProfile = traceProfile;
    }

    public Map<String, NellyRoute> getRoutes() {
        return routes;
    }

    private void setRoutes(Map<String, NellyRoute> routes) {
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

    public String getNellyApiKey() {
        return nellyApiKey;
    }

    private void setNellyApiKey(String nellyApiKey) {
        this.nellyApiKey = nellyApiKey;
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
