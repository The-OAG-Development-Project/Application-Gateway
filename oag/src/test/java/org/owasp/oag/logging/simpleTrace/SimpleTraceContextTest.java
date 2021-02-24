package org.owasp.oag.logging.simpleTrace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SimpleTraceContextTest {

    @Test
    void testSimpleTraceContext(){

        SimpleTraceContext context = new SimpleTraceContext();

        // Check that two ids are not the same
        assertNotEquals(context.establishNew(), context.establishNew());

    }
}