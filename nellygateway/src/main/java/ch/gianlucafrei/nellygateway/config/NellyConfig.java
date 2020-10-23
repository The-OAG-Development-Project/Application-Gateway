package ch.gianlucafrei.nellygateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class NellyConfig {

    public Map<String, AuthProvider> authProviders;
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

}
