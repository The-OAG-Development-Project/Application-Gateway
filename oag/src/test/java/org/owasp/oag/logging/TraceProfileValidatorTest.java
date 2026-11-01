package org.owasp.oag.logging;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraceProfileValidatorTest {

    private final TraceProfileValidator validator =
            new TraceProfileValidator(new TraceContextRegistry(List.of(new NoTraceContextFactory())));

    private static MainConfig configWithTraceType(String type) {
        var config = new MainConfig();
        config.setTraceProfile(new TraceProfile(true, 128, true, 512, true, type, Map.of()));
        return config;
    }

    @Test
    void knownTypeIsValid() {
        assertEquals(List.of(), validator.validate(configWithTraceType("noTraceContext")));
    }

    @Test
    void unknownTypeIsReportedWithKnownTypesAndHint() {
        assertEquals(List.of("TraceProfile: Unknown type 'doesnotexist'. Known types: [noTraceContext]."
                        + " Specify 'noTraceContext' to disable correlation logging."),
                validator.validate(configWithTraceType("doesnotexist")));
    }
}
