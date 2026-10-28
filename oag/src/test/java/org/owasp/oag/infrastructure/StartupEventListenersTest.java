package org.owasp.oag.infrastructure;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.MainConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.ConfigurationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartupEventListenersTest {

    @Test
    void validationErrorsStopStartup() {
        var listeners = new StartupEventListeners();
        listeners.config = new MainConfig();
        listeners.mainConfigValidator = new MainConfigValidator(List.of()) {
            @Override
            public List<String> validate(MainConfig config) {
                return List.of("broken");
            }
        };

        var ex = assertThrows(ConfigurationException.class, listeners::validateConfigurationAfterContextInitialized);

        assertTrue(ex.getMessage().contains("broken"), ex.getMessage());
    }

    @Test
    void validConfigurationStarts() {
        var listeners = new StartupEventListeners();
        listeners.config = new MainConfig();
        listeners.mainConfigValidator = new MainConfigValidator(List.of()) {
            @Override
            public List<String> validate(MainConfig config) {
                return List.of();
            }
        };

        listeners.validateConfigurationAfterContextInitialized();
    }
}
