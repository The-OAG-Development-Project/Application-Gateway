package org.owasp.oag.services.keymgm;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.JwkStoreProfile;
import org.owasp.oag.config.configuration.KeyGeneratorProfile;
import org.owasp.oag.config.configuration.KeyManagementProfile;
import org.owasp.oag.config.configuration.KeyRotationProfile;
import org.owasp.oag.config.configuration.MainConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.owasp.oag.extension.TypedExtensionStub.stub;

class KeyManagementValidatorTest {

    private final KeyManagementValidator validator = new KeyManagementValidator(
            new JwkStoreRegistry(List.of(stub(JwkStore.class, "store"))),
            new KeyGeneratorRegistry(List.of(stub(KeyGenerator.class, "generator"))),
            new KeyRotationRegistry(List.of(stub(KeyRotation.class, "rotation"))));

    private static MainConfig config(String store, String generator, String rotation) {
        var config = new MainConfig();
        config.setKeyManagementProfile(new KeyManagementProfile(new JwkStoreProfile(store, Map.of()),
                new KeyGeneratorProfile(generator, 2048, Map.of()), new KeyRotationProfile(rotation, false, 3600, 3600)));
        return config;
    }

    @Test
    void knownTypesAreValid() {
        assertEquals(List.of(), validator.validate(config("store", "generator", "rotation")));
    }

    @Test
    void unknownTypesAreReportedPerProfile() {
        assertEquals(List.of(
                        "JwkStore: Unknown type 'a'. Known types: [store]",
                        "KeyGenerator: Unknown type 'b'. Known types: [generator]",
                        "KeyRotation: Unknown type 'c'. Known types: [rotation]"),
                validator.validate(config("a", "b", "c")));
    }
}
