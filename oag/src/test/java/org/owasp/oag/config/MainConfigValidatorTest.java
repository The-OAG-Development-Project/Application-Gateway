package org.owasp.oag.config;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.JwkStoreProfile;
import org.owasp.oag.config.configuration.KeyGeneratorProfile;
import org.owasp.oag.config.configuration.KeyManagementProfile;
import org.owasp.oag.config.configuration.KeyRotationProfile;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.SessionBehaviour;
import org.owasp.oag.config.configuration.TraceProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainConfigValidatorTest {

    private static MainConfig validConfig() {
        return new MainConfig(new HashMap<>(), new HashMap<>(), new HashMap<>(), "https://hosturi.org",
                new ArrayList<>(), new SessionBehaviour(3600, 300, "/", "/", "/"),
                new TraceProfile(false, 254, false, 254, false, "w3cTraceContext", null),
                new KeyManagementProfile(new JwkStoreProfile("localRsaJwkStore", new HashMap<>()),
                        new KeyGeneratorProfile("rsaKeyGenerator", 4096, new HashMap<>()),
                        new KeyRotationProfile("defaultKeyRotation", true, 1, 1000)));
    }

    @Test
    void validConfigurationWithoutValidatorsHasNoErrors() {
        var validator = new MainConfigValidator(List.of());

        assertEquals(List.of(), validator.validate(validConfig()));
    }

    @Test
    void errorsOfAllValidatorsAreConcatenated() {
        ConfigValidator first = config -> List.of("a");
        ConfigValidator second = config -> List.of("b", "c");
        var validator = new MainConfigValidator(List.of(first, second));

        assertEquals(List.of("a", "b", "c"), validator.validate(validConfig()));
    }

    @Test
    void structureErrorsStopTheValidators() {
        var config = validConfig();
        config.setTraceProfile(null);
        ConfigValidator failing = c -> {
            throw new AssertionError("validator must not run");
        };
        var validator = new MainConfigValidator(List.of(failing));

        var errors = validator.validate(config);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("traceProfile"), errors.getFirst());
    }

    @Test
    void crossCuttingErrorsStopTheValidators() {
        var config = new MainConfig(new HashMap<>(),
                new HashMap<>(Map.of("route", new GatewayRoute("/abc/**", "https://backend/abc", "missingProfile", false, null))),
                new HashMap<>(), "https://hosturi.org", new ArrayList<>(),
                new SessionBehaviour(3600, 300, "/", "/", "/"), validConfig().getTraceProfile(),
                validConfig().getKeyManagementProfile());
        ConfigValidator failing = c -> {
            throw new AssertionError("validator must not run");
        };

        var errors = new MainConfigValidator(List.of(failing)).validate(config);

        assertEquals(1, errors.size(), "Errors: " + errors);
        assertTrue(errors.getFirst().contains("missingProfile"), errors.getFirst());
    }
}
