package org.owasp.oag.logging.simpleTrace;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SimpleTraceContextTest {

    @Test
    void testSimpleTraceContext(){

        SimpleTraceContext context = new SimpleTraceContext(new MainConfig());
        context.generateNewTraceId();
        String id1 = context.getTraceString();

        context = new SimpleTraceContext(new MainConfig());
        context.generateNewTraceId();
        String id2 = context.getTraceString();
        assertNotEquals(id1, id2);

    }
}