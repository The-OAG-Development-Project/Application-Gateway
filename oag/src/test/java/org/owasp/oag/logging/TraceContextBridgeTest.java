package org.owasp.oag.logging;

import org.junit.jupiter.api.Test;
import org.owasp.oag.exception.TraceException;
import org.owasp.oag.infrastructure.factories.TraceContextFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies how the trace context bridge decides between adopting an inbound trace and generating a
 * new one.
 * <p>
 * The distinction matters beyond the resulting trace id: routing the ordinary "no inbound trace"
 * case through {@link TraceException} makes every first-hop request log, so these tests pin down
 * that {@code applyExistingTrace} is only reached when there is actually a value to apply.
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

    private RecordingTraceContext runBridgeWith(MockServerHttpRequest.BaseBuilder<?> requestBuilder) {

        var traceContext = new RecordingTraceContext();
        TraceContextFactory factory = () -> traceContext;

        var bridge = new TraceContextBridge();
        bridge.setTraceContextFactory(factory);

        bridge.processExchange(MockServerWebExchange.from(requestBuilder.build()));
        return traceContext;
    }

    @Test
    void generatesANewTraceWithoutAttemptingToApplyOneWhenTheHeaderIsAbsent() {

        var traceContext = runBridgeWith(MockServerHttpRequest.get("/"));

        assertFalse(traceContext.applyExistingTraceCalled,
                "applyExistingTrace must not be reached when no traceparent was sent");
        assertTrue(traceContext.generateNewTraceIdCalled);
    }

    @Test
    void generatesANewTraceWithoutAttemptingToApplyOneWhenTheHeaderIsEmpty() {

        // getFirst() yields "" rather than null for a header sent with an empty value
        var traceContext = runBridgeWith(MockServerHttpRequest.get("/").header(MAIN_HEADER, ""));

        assertFalse(traceContext.applyExistingTraceCalled,
                "applyExistingTrace must not be reached for an empty traceparent");
        assertTrue(traceContext.generateNewTraceIdCalled);
    }

    @Test
    void appliesTheInboundTraceWhenAHeaderValueIsPresent() {

        var traceContext = runBridgeWith(MockServerHttpRequest.get("/").header(MAIN_HEADER, "some-trace"));

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

        bridge.processExchange(MockServerWebExchange.from(
                MockServerHttpRequest.get("/").header(MAIN_HEADER, "malformed").build()));

        assertTrue(traceContext.applyExistingTraceCalled);
        assertTrue(traceContext.generateNewTraceIdCalled, "a malformed inbound trace must fall back to a new trace id");
    }
}
