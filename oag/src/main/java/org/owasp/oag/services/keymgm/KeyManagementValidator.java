package org.owasp.oag.services.keymgm;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.extension.TypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that the types configured in {@code keyManagementProfile} are registered: JWK store, key generator
 * and key rotation.
 */
@Component
public class KeyManagementValidator implements ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(KeyManagementValidator.class);

    private final JwkStoreRegistry jwkStores;
    private final KeyGeneratorRegistry keyGenerators;
    private final KeyRotationRegistry keyRotations;

    /**
     * Creates the validator.
     *
     * @param jwkStores     The JWK store registry
     * @param keyGenerators The key generator registry
     * @param keyRotations  The key rotation registry
     */
    public KeyManagementValidator(JwkStoreRegistry jwkStores, KeyGeneratorRegistry keyGenerators,
                                  KeyRotationRegistry keyRotations) {
        this.jwkStores = jwkStores;
        this.keyGenerators = keyGenerators;
        this.keyRotations = keyRotations;
    }

    @Override
    public List<String> validate(MainConfig config) {
        var profile = config.getKeyManagementProfile();
        var errors = new ArrayList<String>();
        validateType(jwkStores, "JwkStore", profile.getJwkStoreProfile().getType(), errors);
        validateType(keyGenerators, "KeyGenerator", profile.getKeyGeneratorProfile().getType(), errors);
        validateType(keyRotations, "KeyRotation", profile.getKeyRotationProfile().getType(), errors);
        return errors;
    }

    private static void validateType(TypeRegistry<?> registry, String name, String type, List<String> errors) {
        registry.unknownTypeError(name, type).ifPresentOrElse(errors::add,
                () -> log.info("Using {} implementation of {}.", name, type));
    }
}
