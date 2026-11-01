package org.owasp.oag.services.keymgm;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.KeyManagementProfile;
import org.owasp.oag.config.configuration.KeyRotationProfile;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyRotationStarterTest {

    static class FakeKeyRotation implements KeyRotation {
        private final String type;
        boolean started;

        FakeKeyRotation(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public void start() {
            started = true;
        }
    }

    @Test
    void startsOnlyTheConfiguredKeyRotation() {
        var configured = new FakeKeyRotation("configured");
        var other = new FakeKeyRotation("other");
        var starter = new KeyRotationStarter(config("configured"), new KeyRotationRegistry(List.of(configured, other)));

        starter.afterSingletonsInstantiated();

        assertTrue(configured.started);
        assertFalse(other.started);
    }

    @Test
    void unknownKeyRotationTypeThrows() {
        var starter = new KeyRotationStarter(config("doesnotexist"), new KeyRotationRegistry(List.of(new FakeKeyRotation("configured"))));

        var ex = assertThrows(ConfigurationException.class, starter::afterSingletonsInstantiated);

        assertTrue(ex.getMessage().contains("[configured]"), ex.getMessage());
    }

    private static MainConfig config(String keyRotationType) {
        var rotation = new KeyRotationProfile();
        rotation.setType(keyRotationType);
        var keyManagement = new KeyManagementProfile();
        keyManagement.setKeyRotationProfile(rotation);
        var config = new MainConfig();
        config.setKeyManagementProfile(keyManagement);
        return config;
    }
}
