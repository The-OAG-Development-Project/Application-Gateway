package ch.gianlucafrei.nellygateway.config;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.customDeserializer.StringEnvironmentVariableDeserializer;
import ch.gianlucafrei.nellygateway.utils.MapTreeUpdater;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileConfigLoader implements NellyConfigLoader {

    private static Logger log = LoggerFactory.getLogger(FileConfigLoader.class);

    @Override
    public NellyConfig loadConfiguration() throws IOException {

        String configPath;

        if(System.getenv("NELLY_CONFIG_PATH") != null)
            configPath = System.getenv("NELLY_CONFIG_PATH");
        else
            configPath = "sample-nelly-config.yaml"; // Default path if we have no config

        log.info("Load nelly configuration from: {}", configPath);

        InputStream defaultConfigStream = NellygatewayApplication.class.getResourceAsStream("/default-config.yaml");
        NellyConfig config = load(defaultConfigStream, configPath);

        log.debug("Configuration successfully loaded");
        return config;
    }

    private NellyConfig load(InputStream defaultSettingsStream, String configPath) throws IOException {

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringEnvironmentVariableDeserializer());
        om.registerModule(module);

        // Load default configuration
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {};
        Map<String, Object> defaultConfigMap = om.readValue(defaultSettingsStream, mapType);

        // Load config
        File userConfigFile = new File(configPath);
        Map<String, Object> userConfigMap = om.readValue(userConfigFile, mapType);

        // Combine default and user config
        Map<String, Object> combinedConfig = MapTreeUpdater.updateMap(defaultConfigMap, userConfigMap);
        String combinedConfigStr = om.writeValueAsString(combinedConfig);
        NellyConfig finalConfig = om.readValue(combinedConfigStr, NellyConfig.class);

        return finalConfig;
    }
}
