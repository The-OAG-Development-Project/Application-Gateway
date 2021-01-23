package ch.gianlucafrei.nellygateway.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class W3cTraceContextTest {

    private final String MDC_NAME = "oagw.CorrId";

    @Test
    void establishNew() {
        String sechzehnNull = "0000000000000000";
        W3cTraceContext ctx = W3cTraceContext.establishNew();
        assertNotNull(ctx);
        assertTrue(ctx.getTraceString().startsWith("00-"));
        assertTrue(ctx.getTraceString().endsWith("-01"));
        assertFalse(ctx.getTraceString().contains(sechzehnNull));
        assertEquals(ctx.getTraceString().length(), 2 + 1 + 32 + 1 + 16 + 1 + 2);
        assertEquals(MDC.get(MDC_NAME), ctx.getTraceString());
    }

    @Test
    void cleanTeardown() {
        W3cTraceContext ctx = W3cTraceContext.establishNew();
        assertNotNull(W3cTraceContext.getCurrent());
        ctx.teardown();
        assertNull(MDC.get(MDC_NAME));
        assertNull(W3cTraceContext.getCurrent());
    }
}