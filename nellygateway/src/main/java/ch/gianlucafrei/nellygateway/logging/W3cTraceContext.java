package ch.gianlucafrei.nellygateway.logging;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.MDC;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implements the W3C Trace Context specification.
 * See https://w3c.github.io/trace-context/
 * <p>
 * It provides functionality to support the respective filter (see W3cTraceContextFilter) and the MDC of the log framework.
 */
public class W3cTraceContext {
    private static final String MDC_CORRELATION_ID_KEY = "oagw.CorrId";
    private static final String W3C_MAIN_RESPONSE_HEADER_NAME = "traceresponse";
    private static final String W3C_MAIN_HEADER_NAME = "traceparent";
    private static final String W3C_SUB_HEADER_NAME = "tracestate";

    private static SecureRandom secRandom = new SecureRandom();
    private static ThreadLocal<W3cTraceContext> currentContext = new ThreadLocal<>();
    private final byte[] version = {0x0}; // we only support version 00
    private final byte[] traceId = new byte[16];
    private final byte[] parentId = new byte[8];
    private final byte[] flags = {0x1}; // we always log -> 01
    private volatile String cachedString = null; // we do not care when we calculate twice, but intermediate results are not wanted.

    private W3cTraceContext() {
        secRandom.nextBytes(traceId);
        secRandom.nextBytes(parentId);
    }

    /**
     * Establishes a new CorrelationId in the system. This is typically used when a new request hits the OAGW.
     * Make sure you also call teardown when the call/scope is finished.
     *
     * @return returns the new unique W3cTraceContext.
     */
    public static W3cTraceContext establishNew() {
        W3cTraceContext traceCtx = new W3cTraceContext();
        currentContext.set(traceCtx);
        MDC.put(MDC_CORRELATION_ID_KEY, traceCtx.getTraceString());
        return traceCtx;
    }

    /**
     * @return the currently valid context or null if none is setup
     */
    public static W3cTraceContext getCurrent() {
        return currentContext.get();
    }

    /**
     * cleanup everything and remove the current context
     */
    public void teardown() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
        currentContext.remove();
    }

    /**
     * @return the header that should be used when a client sends a correlationId
     */
    public String getMainRequestHeader() {
        return W3C_MAIN_HEADER_NAME;
    }

    /**
     * @return the header that should be used when we respond the correlation id upstream
     */
    public String getResponseHeader() {
        return W3C_MAIN_RESPONSE_HEADER_NAME;
    }

    /**
     * @return the current correlation id as a string. in the case of W3CTraceContext this is the traceparent header value.
     */
    public String getTraceString() {
        if (cachedString == null) {
            StringBuilder builder = new StringBuilder(Hex.encodeHexString(version))
                    .append("-").append(Hex.encodeHexString(traceId))
                    .append("-").append(Hex.encodeHexString(parentId))
                    .append("-").append(Hex.encodeHexString(flags));
            cachedString = builder.toString();
        }
        return cachedString;
    }

    /**
     * @return true when a passed in traceparent should be re-used and passed on downstream
     */
    public boolean forwardIncomingTrace() {
        // TODO set based on configuration
        return false;
    }

    /**
     * @return true when also the tracestate passed in should be forwarded downstream
     */
    public boolean acceptAdditionalTraceInfo() {
        // TODO set based on configuration
        return false;
    }

    /**
     * @return true when we send the caller the correlation id (traceparent) we used
     */
    public boolean shouldSendResponse() {
        // TODO: set based on configuration
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(traceId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        W3cTraceContext oth = (W3cTraceContext) other;
        return Arrays.equals(this.traceId, oth.traceId) && Arrays.equals(this.parentId, oth.parentId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("W3cTraceContext{");
        sb.append(getTraceString());
        sb.append('}');
        return sb.toString();
    }
}
