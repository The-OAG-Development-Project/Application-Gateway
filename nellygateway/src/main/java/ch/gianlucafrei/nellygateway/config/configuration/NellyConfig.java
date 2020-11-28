package ch.gianlucafrei.nellygateway.config.configuration;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class NellyConfig {

    private Map<String, LoginProvider> loginProviders;
    private Map<String, NellyRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private String nellyApiKey;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;

    public Map<String, ZuulProperties.ZuulRoute> getRoutesAsZuulRoutes() {

        Map<String, ZuulProperties.ZuulRoute> zuulRoutes = new HashMap<>();

        if (getRoutes() != null) {
            getRoutes().forEach((name, route) -> {
                ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute(route.getPath(), route.getUrl());
                zuulRoute.setId(name);
                zuulRoute.setSensitiveHeaders(new HashSet<>());
                zuulRoutes.put(name, zuulRoute);
            });
        }

        return zuulRoutes;
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
}
