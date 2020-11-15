package ch.gianlucafrei.nellygateway.config;

import ch.gianlucafrei.nellygateway.config.customDeserializer.StringEnvironmentVariableDeserializer;
import ch.gianlucafrei.nellygateway.utils.MapTreeUpdater;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class NellyConfig {

    public Map<String, AuthProvider> authProviders;
    public Map<String, NellyRoute> routes;
    public Map<String, SecurityProfile> securityProfiles;
    public String hostUri;
    public String nellyApiKey;
    public String logoutRedirectUri;
    public List<String> trustedRedirectHosts;

    public static NellyConfig load(String path, String secretsPath) throws IOException {

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());

        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringEnvironmentVariableDeserializer());
        om.registerModule(module);

        File file = new File(path);
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {};
        Map<String, Object> configMap = om.readValue(file, mapType);

        if (secretsPath != null)
        {
            File secretFile = new File(secretsPath);
            Map<String, Object> secretConfigMap = om.readValue(secretFile, mapType);

            configMap = MapTreeUpdater.updateMap(configMap, secretConfigMap);
            // Update object with the secrets
            //config = om.readerForUpdating(config).readValue(secretFile);
        }

        String configWithSecretAsString = om.writeValueAsString(configMap);

        return om.readValue(configWithSecretAsString, NellyConfig.class);
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

}
