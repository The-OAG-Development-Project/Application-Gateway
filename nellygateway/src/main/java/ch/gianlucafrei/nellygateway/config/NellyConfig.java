package ch.gianlucafrei.nellygateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NellyConfig {

    public Map<String, AuthProvider> authProviders;
    public Map<String, NellyRoute> routes;
    public String hostUri;
    public String nellyApiKey;

    public NellyConfig() {

    }

    public static NellyConfig load(String path) throws IOException {

        File file = new File(path);

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        // Mapping the employee from the YAML file to the NellyConfig class
        NellyConfig config = om.readValue(file, NellyConfig.class);

        return config;
    }

    public Map<String, ZuulProperties.ZuulRoute> getRoutesAsZuulRoutes(){

        Map<String, ZuulProperties.ZuulRoute> zuulRoutes = new HashMap<>();

        if(routes != null)
        {
            routes.forEach((name, route) -> {
                ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute(route.path, route.url);
                zuulRoute.setSensitiveHeaders(new HashSet<>());
                zuulRoutes.put(name, zuulRoute);
            });
        }

        return zuulRoutes;
    }

}
