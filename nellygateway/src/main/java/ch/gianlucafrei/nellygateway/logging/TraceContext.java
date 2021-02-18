package ch.gianlucafrei.nellygateway.logging;

/**
 * Represents trace implementations (i.e. correlationID implementations) to simlify log correlation and auditing/tracing
 * what has happened.
 * TraceContexts set up unique correlationId's that are logged with each log statement and are passed to downstream systems
 * to facilitate service spanning log correlation.
 * <p>
 * implementing subclases must ensure, that they setup
 * <p>
 * Tracing is configured in the central configuration as follows:
 */
public interface TraceContext {

    /**
     * The name of the MDC attribute that should be used in order to properly log the trace id with each statement.
     */
    static final String MDC_CORRELATION_ID_KEY = "oag.CorrId";

    /**
     * Establishes a new CorrelationId in the system. This is typically used when a new request hits the OAGW.
     * Make sure you also call teardown when the call/scope is finished.
     *
     * @return returns the new unique W3cTraceContext.
     */
    void establishNew();

    /**
     * Applies the passed in data and changes the traceId to match the data passed in.
     * If the passed in data is not in a valid format/not acceptable, logs a warning and continues with the already setup trace id.
     *
     * @param primaryTraceInfo   trace id data from the primary trace header (correlation if header), may be null (in which case it is ignored).
     * @param secondaryTraceInfo trace id data from a potential secondary trace header, may be null (in which case it is ignored).
     */
    void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo);

    /**
     * cleanup everything and remove the current context
     */
    void teardown();

    /**
     * @return the header that should be used when a client sends a correlationId
     */
    String getMainRequestHeader();

    /**
     * @return the header that should be used when a client sends secondary information (i.e. vendor specific) for a trace.
     * if null is returned, no secondary trace info is available for the implementation.
     */
    String getSecondaryRequestHeader();

    /**
     * @return the header that should be used when we respond the correlation id upstream
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
     * @return true when the trace system was setup for the call.
     */
    boolean hasCurrentTraceId();

    /**
     * @return true when a received (incoming trace header) should be forwarded to downstream systems.
     */
    boolean forwardIncomingTrace();

    /**
     * @return true when also incomming secondary trace information should be forwarded to downstream systems.
     */
    boolean acceptAdditionalTraceInfo();

    /**
     * @return true when we should send the caller the correlation id we used/created as a response header.
     */
    boolean sendTraceResponse();
}
