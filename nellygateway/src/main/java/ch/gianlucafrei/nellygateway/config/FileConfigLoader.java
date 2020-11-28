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
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileConfigLoader implements NellyConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(FileConfigLoader.class);

    @Value("${nelly.configPath}")
    private String configPath;


    @Override
    public NellyConfig loadConfiguration() throws IOException {

        log.info("Load nelly configuration from: {}", configPath);

        File userConfigFile = new File(configPath);
        InputStream userConfigInputStream = new FileInputStream(userConfigFile);
        InputStream defaultConfigStream = NellygatewayApplication.class.getResourceAsStream("/default-config.yaml");

        NellyConfig config = load(defaultConfigStream, userConfigInputStream);

        log.debug("Configuration successfully loaded");
        return config;
    }

    protected NellyConfig load(InputStream defaultSettingsStream, InputStream userConfigInputStream) throws IOException {

        // Instantiating a new ObjectMapper as a YAMLFactory
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StringEnvironmentVariableDeserializer());
        om.registerModule(module);

        // Load default configuration
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {
        };
        Map<String, Object> defaultConfigMap = om.readValue(defaultSettingsStream, mapType);

        // Load config
        Map<String, Object> userConfigMap = om.readValue(userConfigInputStream, mapType);

        // Combine default and user config
        Map<String, Object> combinedConfig = MapTreeUpdater.updateMap(defaultConfigMap, userConfigMap);
        String combinedConfigStr = om.writeValueAsString(combinedConfig);
        NellyConfig finalConfig = om.readValue(combinedConfigStr, NellyConfig.class);

        return finalConfig;
    }
}
