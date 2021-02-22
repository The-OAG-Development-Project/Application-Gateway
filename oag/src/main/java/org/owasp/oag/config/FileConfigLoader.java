package org.owasp.oag.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.customDeserializer.StringEnvironmentVariableDeserializer;
import org.owasp.oag.utils.MapTreeUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileConfigLoader implements ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(FileConfigLoader.class);

    @Value("${nelly.configPath}")
    private String configPath;


    @Override
    public MainConfig loadConfiguration() throws IOException {

        log.info("Load nelly configuration from: {}", configPath);

        File userConfigFile = new File(configPath);
        InputStream userConfigInputStream = new FileInputStream(userConfigFile);
        InputStream defaultConfigStream = OWASPApplicationGatewayApplication.class.getResourceAsStream("/default-config.yaml");

        MainConfig config = load(defaultConfigStream, userConfigInputStream);

        log.debug("Configuration successfully loaded");
        return config;
    }

    protected MainConfig load(InputStream defaultSettingsStream, InputStream userConfigInputStream) throws IOException {

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
        MainConfig finalConfig = om.readValue(combinedConfigStr, MainConfig.class);

        return finalConfig;
    }
}
