package org.owasp.oag.logging;

import java.util.Map;

/**
 * The tracing decision for one request, expressed as plain data.
 *
 * @param traceId           the trace id of the request, used for log correlation
 * @param downstreamHeaders headers to add to the request forwarded to the downstream system, empty if
 *                          the trace profile does not send the trace downstream
 * @param responseHeaders   headers to add to the response returned to the caller, empty if the trace
 *                          profile does not send the trace back upstream
 */
public record TraceHeaders(String traceId, Map<String, String> downstreamHeaders, Map<String, String> responseHeaders) {

    /**
     * Copies both header maps so the value object cannot be mutated through the reference it was
     * built from. This matters because the instance is shared through the request attributes, where
     * any filter could otherwise alter another filter's view of the tracing decision.
     */
    public TraceHeaders {
        downstreamHeaders = Map.copyOf(downstreamHeaders);
        responseHeaders = Map.copyOf(responseHeaders);
    }
}
