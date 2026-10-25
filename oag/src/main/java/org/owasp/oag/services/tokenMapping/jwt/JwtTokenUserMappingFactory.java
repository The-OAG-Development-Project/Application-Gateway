package org.owasp.oag.services.tokenMapping.jwt;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.jwt.JwtSignerFactory;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

/**
 * Factory for creating JWT token user mapping implementations.
 * This component creates and configures JWT-based user mappers
 * that transform user information into JWT tokens for downstream services.
 */
@Component
public class JwtTokenUserMappingFactory implements UserMappingFactory {

    /**
     * The user mapping type handled by this factory.
     */
    public static final String TYPE = "jwtToken";

    @Override
    public String type() {
        return TYPE;
    }

    private final ApplicationContext context;
    private final GlobalClockSource clockSource;
    private final MainConfig mainConfig;

    /**
     * Creates the factory.
     *
     * @param context     Application context for accessing beans
     * @param clockSource Clock source for consistent time handling
     * @param mainConfig  Main configuration for the application
     */
    public JwtTokenUserMappingFactory(ApplicationContext context, GlobalClockSource clockSource, MainConfig mainConfig) {
        this.context = context;
        this.clockSource = clockSource;
        this.mainConfig = mainConfig;
    }

    /**
     * Creates a new JWT token user mapper from the provided settings.
     *
     * @param settings Map containing configuration settings for the JWT token user mapper
     * @return A configured UserMapper instance that uses JWT tokens
     * @throws ConfigurationException If the settings cannot be deserialized or the signature implementation cannot be found
     */
    @Override
    public UserMapper load(Map<String, Object> settings) {

        // Load settings
        JwtTokenUserMappingSettings jwtTokenUserMappingSettings;
        try {
            jwtTokenUserMappingSettings = SettingsUtils.settingsFromMap(settings, JwtTokenUserMappingSettings.class);
        } catch (Exception ex) {
            throw new ConfigurationException("Cannot deserialize jwt-mapping settings", ex);
        }
        jwtTokenUserMappingSettings.requireValidSettings();

        // Init jwt signer
        var factoryName = jwtTokenUserMappingSettings.signatureImplementation + JWT_SIGNER_FACTORY_BEAN_POSTFIX;
        try {
            var factory = context.getBean(factoryName, JwtSignerFactory.class);
            // Create user mapper
            return new JwtTokenUserMapper(factory, clockSource, jwtTokenUserMappingSettings, mainConfig.getHostUri());
        } catch (BeansException be) {
            throw new ConfigurationException("No implementation found for signature implementation: " + jwtTokenUserMappingSettings.signatureImplementation+". The Bean is expected to be named: "+factoryName, be);
        }
    }
}
