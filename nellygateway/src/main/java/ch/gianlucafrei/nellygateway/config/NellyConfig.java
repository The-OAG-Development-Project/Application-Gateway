package ch.gianlucafrei.nellygateway.config;

import ch.gianlucafrei.nellygateway.config.customDeserializer.StringEnvironmentVariableDeserializer;
import ch.gianlucafrei.nellygateway.utils.MapTreeUpdater;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class NellyConfig {

    public Map<String, AuthProvider> authProviders;
    public Map<String, LoginProvider> loginProviders;
    public Map<String, NellyRoute> routes;
    public Map<String, SecurityProfile> securityProfiles;
    public String hostUri;
    public String nellyApiKey;
    public String logoutRedirectUri;
    public List<String> trustedRedirectHosts;
    public SessionBehaviour sessionBehaviour;

    public static NellyConfig load(URI defaultSettingsURI, String configPath) throws IOException {

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringEnvironmentVariableDeserializer());
        om.registerModule(module);

        // Load default configuration
        File file = new File(defaultSettingsURI);
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {};
        Map<String, Object> defaultConfigMap = om.readValue(file, mapType);

        // Load config
        File userConfigFile = new File(configPath);
        Map<String, Object> userConfigMap = om.readValue(userConfigFile, mapType);

        // Combine default and user config
        Map<String, Object> combinedConfig = MapTreeUpdater.updateMap(defaultConfigMap, userConfigMap);
        String combinedConfigStr = om.writeValueAsString(combinedConfig);
        NellyConfig finalConfig = om.readValue(combinedConfigStr, NellyConfig.class);

        return finalConfig;
    }

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
