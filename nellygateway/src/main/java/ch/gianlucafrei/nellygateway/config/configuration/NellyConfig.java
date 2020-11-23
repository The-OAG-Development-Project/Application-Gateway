package ch.gianlucafrei.nellygateway.config.configuration;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class NellyConfig {

    private Map<String, LoginProvider> loginProviders;
    private Map<String, NellyRoute> routes;
    private Map<String, SecurityProfile> securityProfiles;
    private String hostUri;
    private String nellyApiKey;
    private List<String> trustedRedirectHosts;
    private SessionBehaviour sessionBehaviour;

    public Map<String, ZuulProperties.ZuulRoute> getRoutesAsZuulRoutes(){

        Map<String, ZuulProperties.ZuulRoute> zuulRoutes = new HashMap<>();

        if(getRoutes() != null)
        {
            getRoutes().forEach((name, route) -> {
                ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute(route.getPath(), route.getUrl());
                zuulRoute.setId(name);
                zuulRoute.setSensitiveHeaders(new HashSet<>());
                zuulRoutes.put(name, zuulRoute);
            });
        }

        return zuulRoutes;
    }

    public boolean isHttpsHost(){

        return getHostUri().startsWith("https://");
    }

    public Map<String, LoginProvider> getLoginProviders() {
        return loginProviders;
    }

    private void setLoginProviders(Map<String, LoginProvider> loginProviders) {
        this.loginProviders = loginProviders;
    }

    public Map<String, NellyRoute> getRoutes() {
        return routes;
    }

    private void setRoutes(Map<String, NellyRoute> routes) {
        this.routes = routes;
    }

    public Map<String, SecurityProfile> getSecurityProfiles() {
        return securityProfiles;
    }

    private void setSecurityProfiles(Map<String, SecurityProfile> securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    public String getHostUri() {
        return hostUri;
    }

    private void setHostUri(String hostUri) {
        this.hostUri = hostUri;
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
