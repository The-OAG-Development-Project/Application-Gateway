package org.owasp.oag.logging;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that the configured {@code traceProfile.type} is a registered {@link TraceContextFactory}.
 */
@Component
public class TraceProfileValidator implements ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(TraceProfileValidator.class);

    private final TraceContextRegistry registry;

    /**
     * Creates the validator.
     *
     * @param registry The trace context registry
     */
    public TraceProfileValidator(TraceContextRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> validate(MainConfig config) {
        var type = config.getTraceProfile().getType();
        var error = registry.unknownTypeError("TraceProfile", type)
                .map(message -> message + ". Specify 'noTraceContext' to disable correlation logging.");
        if (error.isEmpty())
            log.info("Using log trace / correlation implementation of {}.", type);
        return error.stream().toList();
    }
}
