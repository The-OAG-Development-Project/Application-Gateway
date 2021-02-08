package ch.gianlucafrei.nellygateway.logging.w3ctrace;

import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.logging.TraceException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class W3cTraceContextTest {

    @Test
    void establishNew() {
        String sechzehnNull = "0000000000000000";

        W3cTraceContext ctx = new W3cTraceContext();
        ctx.establishNew();
        assertNotNull(ctx);
        assertTrue(ctx.getTraceString().startsWith("00-"));
        assertTrue(ctx.getTraceString().endsWith("-01"));
        assertFalse(ctx.getTraceString().contains(sechzehnNull));
        assertEquals(ctx.getTraceString().length(), 2 + 1 + 32 + 1 + 16 + 1 + 2);
        assertEquals(MDC.get(TraceContext.MDC_CORRELATION_ID_KEY), ctx.getTraceString());
        ctx.teardown();
    }

    @Test
    void cleanTeardown() {
        W3cTraceContext ctx = new W3cTraceContext();
        ctx.establishNew();
        assertNotNull(ctx.getTraceString());
        ctx.teardown();
        assertNull(MDC.get(TraceContext.MDC_CORRELATION_ID_KEY));
        assertThrows(TraceException.class, () -> ctx.getTraceString());
    }
}