package org.owasp.oag.logging;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.TraceProfile;
import org.owasp.oag.exception.TraceException;
import org.owasp.oag.infrastructure.factories.TraceContextFactory;
import org.owasp.oag.logging.w3ctrace.W3cTraceContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies how the trace context bridge decides between adopting an inbound trace and generating a
 * new one, and the {@link TraceHeaders} it produces from that decision.
 * <p>
 * The distinction matters beyond the resulting trace id: routing the ordinary "no inbound trace"
 * case through {@link TraceException} makes every first-hop request log, so the first four tests
 * pin down that {@code applyExistingTrace} is only reached when there is actually a value to apply.
 */
class TraceContextBridgeTest {

    private static final String MAIN_HEADER = "traceparent";
    private static final String SECONDARY_HEADER = "tracestate";

    /**
     * Records which of the two trace-establishing methods the bridge called.
     */
    private static class RecordingTraceContext implements TraceContext {

        private boolean applyExistingTraceCalled;
        private boolean generateNewTraceIdCalled;
        private String appliedPrimary;

        /** When set, applyExistingTrace rejects the value the way a malformed traceparent would. */
        private boolean rejectAppliedTrace;

        @Override
        public void generateNewTraceId() {
            generateNewTraceIdCalled = true;
        }

        @Override
        public void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo) {
            applyExistingTraceCalled = true;
            appliedPrimary = primaryTraceInfo;
            if (rejectAppliedTrace) {
                throw new TraceException("traceparent format wrong.");
            }
            if (primaryTraceInfo == null || primaryTraceInfo.isEmpty()) {
                throw new TraceException("traceparent not provided.");
            }
        }

        @Override
        public boolean forwardIncomingTrace() {
            return true;
        }

        @Override
        public boolean acceptAdditionalTraceInfo() {
            return true;
        }

        @Override
        public boolean sendTraceDownstream() {
            return false;
        }

        @Override
        public boolean sendTraceResponse() {
            return false;
        }

        @Override
        public String getMainRequestHeader() {
            return MAIN_HEADER;
        }

        @Override
        public String getSecondaryRequestHeader() {
            return SECONDARY_HEADER;
        }

        @Override
        public String getResponseHeader() {
            return "traceresponse";
        }

        @Override
        public String getTraceString() {
            return "trace-string";
        }

        @Override
        public String getSecondaryTraceInfoString() {
            return "secondary-trace-string";
        }

        @Override
        public String getTraceResponseString() {
            return "trace-response-string";
        }
    }

    private RecordingTraceContext runBridgeWith(String primary, String secondary) {

        var traceContext = new RecordingTraceContext();
        TraceContextFactory factory = () -> traceContext;

        var bridge = new TraceContextBridge();
        bridge.setTraceContextFactory(factory);

        bridge.createTraceHeaders(primary, secondary);
        return traceContext;
    }

    @Test
    void generatesANewTraceWithoutAttemptingToApplyOneWhenTheHeaderIsAbsent() {

        var traceContext = runBridgeWith(null, null);

        assertFalse(traceContext.applyExistingTraceCalled,
                "applyExistingTrace must not be reached when no traceparent was sent");
        assertTrue(traceContext.generateNewTraceIdCalled);
    }

    @Test
    void generatesANewTraceWithoutAttemptingToApplyOneWhenTheHeaderIsEmpty() {

        // the header accessor yields "" rather than null for a header sent with an empty value
        var traceContext = runBridgeWith("", null);

        assertFalse(traceContext.applyExistingTraceCalled,
                "applyExistingTrace must not be reached for an empty traceparent");
        assertTrue(traceContext.generateNewTraceIdCalled);
    }

    @Test
    void appliesTheInboundTraceWhenAHeaderValueIsPresent() {

        var traceContext = runBridgeWith("some-trace", null);

        assertTrue(traceContext.applyExistingTraceCalled);
        assertEquals("some-trace", traceContext.appliedPrimary);
        assertFalse(traceContext.generateNewTraceIdCalled);
    }

    @Test
    void fallsBackToANewTraceWhenApplyingAMalformedInboundTraceFails() {

        var traceContext = new RecordingTraceContext();
        traceContext.rejectAppliedTrace = true;

        var bridge = new TraceContextBridge();
        bridge.setTraceContextFactory(() -> traceContext);

        bridge.createTraceHeaders("malformed", null);

        assertTrue(traceContext.applyExistingTraceCalled);
        assertTrue(traceContext.generateNewTraceIdCalled, "a malformed inbound trace must fall back to a new trace id");
    }

    /**
     * Builds a real {@link W3cTraceContext} wired to a {@link MainConfig} with the given trace
     * profile settings, using the production header names and formats rather than the stub.
     */
    private W3cTraceContext newW3cTraceContext(boolean forwardIncomingTrace, boolean acceptAdditionalTraceInfo, boolean sendTraceResponse) {

        var traceProfile = new TraceProfile(forwardIncomingTrace, 128, acceptAdditionalTraceInfo, 512,
                sendTraceResponse, "w3cTraceContext", Map.of());
        var config = new MainConfig();
        config.setTraceProfile(traceProfile);

        var traceContext = new W3cTraceContext();
        ReflectionTestUtils.setField(traceContext, "config", config);
        return traceContext;
    }

    private TraceHeaders createTraceHeaders(TraceContext traceContext, String primary, String secondary) {

        var bridge = new TraceContextBridge();
        bridge.setTraceContextFactory(() -> traceContext);
        return bridge.createTraceHeaders(primary, secondary);
    }

    @Test
    void createsANewTraceWhenNothingIsIncoming() {

        var traceContext = newW3cTraceContext(true, true, true);

        var traceHeaders = createTraceHeaders(traceContext, null, null);

        assertTrue(traceHeaders.traceId().startsWith("00-"));
        assertEquals(traceHeaders.traceId(), traceHeaders.downstreamHeaders().get(MAIN_HEADER));
        assertEquals(traceHeaders.traceId(), traceHeaders.responseHeaders().get("traceresponse"));
    }

    @Test
    void adoptsAValidIncomingTrace() {

        var traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
        var traceState = "rojo=00f067aa0ba902b7,congo=t61rcWkgMzE";
        var traceContext = newW3cTraceContext(true, true, true);

        var traceHeaders = createTraceHeaders(traceContext, traceParent, traceState);

        assertEquals(traceParent, traceHeaders.traceId());
        assertEquals(traceParent, traceHeaders.downstreamHeaders().get(MAIN_HEADER));
        assertEquals(traceState, traceHeaders.downstreamHeaders().get(SECONDARY_HEADER));
    }

    @Test
    void replacesAnInvalidIncomingTrace() {

        var invalidTraceParent = "00-00000000000000000000000000000000-b7ad6b7169203331-01";
        var traceContext = newW3cTraceContext(true, true, true);

        var traceHeaders = createTraceHeaders(traceContext, invalidTraceParent, null);

        assertNotEquals(invalidTraceParent, traceHeaders.traceId());
        assertEquals(traceHeaders.traceId(), traceHeaders.downstreamHeaders().get(MAIN_HEADER));
    }

    @Test
    void ignoresAValidIncomingTraceWhenForwardingIsDisabled() {

        var traceParent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
        var traceContext = newW3cTraceContext(false, true, true);

        var traceHeaders = createTraceHeaders(traceContext, traceParent, null);

        assertNotEquals(traceParent, traceHeaders.traceId());
    }

    @Test
    void producesNoHeadersWhenTracingIsOff() {

        var bridge = new TraceContextBridge();
        bridge.setTraceContextFactory(NoTraceContext::new);

        var traceHeaders = bridge.createTraceHeaders(null, null);

        assertTrue(traceHeaders.downstreamHeaders().isEmpty());
        assertTrue(traceHeaders.responseHeaders().isEmpty());
        assertFalse(traceHeaders.traceId().isEmpty());
    }
}
