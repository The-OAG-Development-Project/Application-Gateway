package org.owasp.oag.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;
import org.owasp.oag.OWASPApplicationGatewayApplication;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.customDeserializer.StringEnvironmentVariableDeserializer;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.utils.MapTreeUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements the default way how OAG loads the OAG configuration.
 * If the given path of the oag configuration starts with https:// the configuration file is loaded from the given url.
 * Otherwise, it is loaded from the local disk.
 *
 * The loaded configuration file is then merged with the default configuration whereas the user configuration has precedence
 * over the settings from the default configuration file.
 *
 */
public class FileConfigLoader implements ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(FileConfigLoader.class);

    private final String configPath;
    private final HttpClient httpClient;
    private boolean allowUnsafeHttp = false;

    /**
     * Creates a new FileConfigLoader which will load the given configuration file (disk or https)
     * @param configPath Path of oag config file
     */
    public FileConfigLoader(String configPath) {
        this.configPath = configPath;
        this.httpClient = HttpClient.newHttpClient();

    }

    /**
     * Creates a new FileConfigLoader which will load the given configuration file (disk or https)
     * @param configPath Path of oag config file
     * @param httpClient Custom httpClient
     */
    public FileConfigLoader(String configPath, HttpClient httpClient){
        this.configPath = configPath;
        this.httpClient = httpClient;
    }

    /**
     * Loads the configuration file from the given configuration path (in constructor) and merges it with the
     * default configuration. It is not validated if the configuration file has semantic errors.
     * @return MainConfig object containing the merged OAG configuration
     * @throws IOException if the configuration file cannot be loaded or the file format is invalid (not yaml)
     */
    @Override
    public MainConfig loadConfiguration() throws IOException {

        log.info("Load configuration from: {}", configPath);

        InputStream userConfigInputStream;
        if (configPath.startsWith("https://")) {
            userConfigInputStream = loadRemoteConfigFile();
        } else if (configPath.startsWith("http://") && this.allowUnsafeHttp) {
            userConfigInputStream = loadRemoteConfigFile();
        } else {
            userConfigInputStream = loadConfigFromFile();
        }

        InputStream defaultConfigStream = OWASPApplicationGatewayApplication.class.getResourceAsStream("/default-config.yaml");
        MainConfig config = mergeConfiguration(defaultConfigStream, userConfigInputStream);

        log.debug("Configuration successfully loaded");
        return config;
    }

    /**
     * Loads an input stream of the configuration file via https or http
     * @return InputStream containing the remote configuration file
     * @throws IOException if there is an error loading the remote configuration file
     */
    @NotNull
    protected InputStream loadRemoteConfigFile() throws IOException {

        log.debug("Load configuration file via https: {}", configPath);

        try{
            var request = HttpRequest.newBuilder(
                    URI.create(configPath))
                    .header("accept", "application/x-yaml")
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            log.info("Requested configuration file via https. status_code={}, url={}", response.statusCode(), configPath);

            return response.body();

        }catch (InterruptedException ex){

            throw new ConfigurationException("Could not load configuration via https", ex);
        }
    }

    /**
     * Loads an input stream of the configuration file from disk
     * @return InputStream containing the configuration file from disk
     * @throws FileNotFoundException if the configuration file cannot be found
     */
    @NotNull
    protected InputStream loadConfigFromFile() throws FileNotFoundException {

        log.debug("Load configuration file from file: {}", configPath);
        File userConfigFile = new File(configPath);
        InputStream userConfigInputStream = new FileInputStream(userConfigFile);
        return userConfigInputStream;
    }

    /**
     * Deserializes and merges the default and user configuration
     * @param defaultSettingsStream Input stream of default configuration file
     * @param userConfigInputStream Input stream of user configuration file
     * @return MainConfig object containing the merged configuration
     * @throws IOException if there is an error reading or parsing the configuration files
     */
    @NotNull
    protected MainConfig mergeConfiguration(InputStream defaultSettingsStream, InputStream userConfigInputStream) throws IOException {

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

    /**
     * Enables support to load configuration file via unsafe https connections.
     */
    public void enableUnsafeHttp() {
        this.allowUnsafeHttp = true;
    }
}
