package ch.gianlucafrei.nellygateway.config.configuration;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class NellyConfig {

    public Map<String, LoginProvider> loginProviders;
    public Map<String, NellyRoute> routes;
    public Map<String, SecurityProfile> securityProfiles;
    public String hostUri;
    public String nellyApiKey;
    public String logoutRedirectUri;
    public List<String> trustedRedirectHosts;
    public SessionBehaviour sessionBehaviour;



    public Map<String, ZuulProperties.ZuulRoute> getRoutesAsZuulRoutes(){

        Map<String, ZuulProperties.ZuulRoute> zuulRoutes = new HashMap<>();

        if(routes != null)
        {
            routes.forEach((name, route) -> {
                ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute(route.path, route.url);
                zuulRoute.setId(name);
                zuulRoute.setSensitiveHeaders(new HashSet<>());
                zuulRoutes.put(name, zuulRoute);
            });
        }

        return zuulRoutes;
    }

    public String getHostname(){

        try {
            URL url = new URL(hostUri);
            return url.getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Host Uri from config is not a valid URL");
        }
    }

    public boolean isHttpsHost(){

        return hostUri.startsWith("https://");
    }

}
