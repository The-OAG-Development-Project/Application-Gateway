package org.owasp.oag.services.tokenMapping.jwt;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.crypto.jwt.JwtSignerRegistry;
import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.owasp.oag.utils.SettingsUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    private final JwtSignerRegistry signerRegistry;
    private final GlobalClockSource clockSource;
    private final MainConfig mainConfig;

    /**
     * Creates the factory.
     *
     * @param signerRegistry Registry of the JWT signer implementations
     * @param clockSource    Clock source for consistent time handling
     * @param mainConfig     Main configuration for the application
     */
    public JwtTokenUserMappingFactory(JwtSignerRegistry signerRegistry, GlobalClockSource clockSource, MainConfig mainConfig) {
        this.signerRegistry = signerRegistry;
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
        var signerFactory = signerRegistry.get(jwtTokenUserMappingSettings.signatureImplementation);
        return new JwtTokenUserMapper(signerFactory, clockSource, jwtTokenUserMappingSettings, mainConfig.getHostUri());
    }
}
