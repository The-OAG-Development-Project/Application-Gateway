package ch.gianlucafrei.nellygateway.logging.simpleTrace;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.TraceProfile;
import ch.gianlucafrei.nellygateway.logging.TraceContext;
import ch.gianlucafrei.nellygateway.logging.TraceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.MDC;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class SimpleTraceContextTest {

    @BeforeEach
    void resetAllStaticInit() throws NoSuchFieldException, IllegalAccessException {
        Field staticField = SimpleTraceContext.class.getDeclaredField("appliedHeaderName");
        staticField.setAccessible(true);
        staticField.set(null, null);
    }

    @Test
    void a0EstablishNew() {
        SimpleTraceContext ctx = new SimpleTraceContext();
        ctx.establishNew();
        assertTrue(ctx.hasCurrentTraceId());
        assertNotNull(ctx.getTraceString());
        assertTrue(ctx.getTraceString().length() > 10);
        assertEquals(MDC.get(TraceContext.MDC_CORRELATION_ID_KEY), ctx.getTraceString());
        ctx.teardown();
    }

    @Test
    void a1CleanTeardown() {
        SimpleTraceContext ctx = new SimpleTraceContext();
        ctx.establishNew();
        ctx.teardown();
        assertNull(MDC.get(TraceContext.MDC_CORRELATION_ID_KEY));
        assertThrows(TraceException.class, () -> ctx.getTraceString());
    }

    @Test
    void a2ApplyIncoming() throws NoSuchFieldException, IllegalAccessException {
        NellyConfig config = new NellyConfig();
        TraceProfile tp = new TraceProfile();
        tp.setMaxLengthIncomingTrace(32);
        config.setTraceProfile(tp);

        SimpleTraceContext ctx = new SimpleTraceContext();
        Field nameField = ctx.getClass().getDeclaredField("config");
        nameField.setAccessible(true);
        nameField.set(ctx, config);

        String incoming = "myTest";
        ctx.applyExistingTrace(incoming, null);
        assertEquals(incoming, ctx.getTraceString());
    }

    @Test
    void a3DefaultHeader() throws NoSuchFieldException, IllegalAccessException {
        NellyConfig config = new NellyConfig();
        TraceProfile tp = new TraceProfile();
        tp.setMaxLengthIncomingTrace(32);
        config.setTraceProfile(tp);

        SimpleTraceContext ctx = new SimpleTraceContext();
        Field nameField = ctx.getClass().getDeclaredField("config");
        nameField.setAccessible(true);
        nameField.set(ctx, config);

        assertEquals("X-Correlation-Id", ctx.getMainRequestHeader());
    }

    @Test()
    void a4CustomHeader() throws NoSuchFieldException, IllegalAccessException {
        String testHeaderName = "X-test-header";

        NellyConfig config = new NellyConfig();
        TraceProfile tp = new TraceProfile();
        tp.getTraceImplSpecificSettings().put("headerName", testHeaderName);
        config.setTraceProfile(tp);

        SimpleTraceContext ctx = new SimpleTraceContext();
        Field nameField = ctx.getClass().getDeclaredField("config");
        nameField.setAccessible(true);
        nameField.set(ctx, config);

        assertEquals(testHeaderName, ctx.getMainRequestHeader());
    }
}