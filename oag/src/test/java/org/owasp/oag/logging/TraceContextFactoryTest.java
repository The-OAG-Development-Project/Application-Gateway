package org.owasp.oag.logging;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.logging.simpleTrace.SimpleTraceContext;
import org.owasp.oag.logging.simpleTrace.SimpleTraceContextFactory;
import org.owasp.oag.logging.w3ctrace.W3cTraceContext;
import org.owasp.oag.logging.w3ctrace.W3cTraceContextFactory;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TraceContextFactoryTest {

    private final MainConfig config = new MainConfig();
    private final TraceContextRegistry registry = new TraceContextRegistry(List.of(
            new W3cTraceContextFactory(config), new SimpleTraceContextFactory(config), new NoTraceContextFactory()));

    @Test
    void registersBuiltInTraceTypes() {
        assertEquals(Set.of("w3cTraceContext", "simpleTraceContext", "noTraceContext"), registry.types());
    }

    @Test
    void factoriesCreateAFreshContextOfTheirTypePerCall() {
        assertCreatesFreshInstances("w3cTraceContext", W3cTraceContext.class);
        assertCreatesFreshInstances("simpleTraceContext", SimpleTraceContext.class);
        assertCreatesFreshInstances("noTraceContext", NoTraceContext.class);
    }

    private void assertCreatesFreshInstances(String type, Class<? extends TraceContext> expectedClass) {
        var factory = registry.get(type);

        var first = factory.create();
        var second = factory.create();

        assertInstanceOf(expectedClass, first);
        assertNotSame(first, second);
    }
}
