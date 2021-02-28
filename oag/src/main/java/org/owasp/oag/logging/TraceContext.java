package org.owasp.oag.logging;

import org.springframework.web.server.ServerWebExchange;

/**
 * Represents trace implementations (i.e. correlationID implementations) to simlify log correlation and auditing/tracing
 * what has happened.
 * TraceContexts set up unique correlationId's that are logged with each log statement and are passed to downstream systems
 * to facilitate service spanning log correlation.
 * <p>
 * Tracing is configured in the central configuration as documented on the project wiki.
 */
public interface TraceContext {

    /**
     * Establishes a new CorrelationId / traceId in the system. This is typically used when a new request hits the OAGW.
     * The framework calls this method when it figured out that no traceId can be taken over from the upstream system.
     * Implementations must ensure that they have setup/generated a valid new traceid/internal state.
     */
    void generateNewTraceId();

    /**
     * Applies the passed in data and changes the traceId to match the data passed in.
     * If the passed in data is not in a valid format/not acceptable, logs a warning and continues with the already setup trace id or establishes a new one as required.
     *
     * @param primaryTraceInfo   trace id data from the primary trace header (correlation if header), may be null (in which case it is ignored).
     * @param secondaryTraceInfo trace id data from a potential secondary trace header, may be null (in which case it is ignored).
     */
    void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo);

    /**
     * @return true when we should send trace information to downstream systems. All implementations except noTrace should most likely return true here.
     */
    boolean sendTraceDownstream();

    /**
     * @return the header name upstream systems (clients) use when sending main trace information.
     */
    String getMainRequestHeader();

    /**
     * @return the header name upstream systems (clients) use hen sending secondary trace information.
     * if null is returned, no secondary trace info is available/accepted for/by the implementation.
     */
    String getSecondaryRequestHeader();

    /**
     * @return the header name that should be used when we respond the used main traceid to the calling upstream back.
     */
    String getResponseHeader();

    /**
     * @return the current correlation id (value) as a string. For the main trace header.
     * @throws TraceException when no trace context is setup.
     */
    String getTraceString();

    /**
     * @return the secondary trace info data. For the secondary trace header.
     * @throws TraceException when no trace context is setup.
     */
    String getSecondaryTraceInfoString();

    /**
     * @return the trace response data that should be sent back to the caller (when configured to respond).
     * @throws TraceException when no trace context is setup.
     */
    String getTraceResponseString();

    /**
     * @return true when a received (incoming trace header) should be forwarded to downstream systems.
     */
    boolean forwardIncomingTrace();

    /**
     * @return true when also incoming secondary trace information should be forwarded to downstream systems.
     */
    boolean acceptAdditionalTraceInfo();

    /**
     * @return true when we should send the caller the trace id we used/created as a response header back.
     */
    boolean sendTraceResponse();
}
