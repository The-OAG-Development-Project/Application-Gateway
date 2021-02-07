package ch.gianlucafrei.nellygateway.logging.w3ctrace;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.MDC;

import java.security.SecureRandom;
import java.util.Arrays;

import static ch.gianlucafrei.nellygateway.logging.TraceContext.MDC_CORRELATION_ID_KEY;

/**
 * Implements the W3C Trace Context specification. Holds the state required by W3CTraceContext.
 * See https://w3c.github.io/trace-context/
 */
class W3cTraceContextState {

    private static SecureRandom secRandom = new SecureRandom();

    // Trace Info (traceparent)
    private final byte[] version = {0x0}; // we only support version 00
    private final byte[] traceId = new byte[16];
    private final byte[] parentId = new byte[8];
    private final byte[] flags = {0x1}; // we always log -> 01
    private volatile String cachedString = null; // we do not care when we calculate twice, but intermediate results are not wanted.
    // Secondary Trace Info (tracestate))
    private String secondaryTraceInfoString = null;

    W3cTraceContextState() {
        secRandom.nextBytes(traceId);
        secRandom.nextBytes(parentId);
        MDC.put(MDC_CORRELATION_ID_KEY, getTraceString());
    }

    /**
     * cleanup everything and remove the current context
     */
    public void teardown() {
        MDC.remove(MDC_CORRELATION_ID_KEY);
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
     * @return the tracestate string.
     */
    public String getSecondaryTraceInfoString() {
        return secondaryTraceInfoString;
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
        W3cTraceContextState oth = (W3cTraceContextState) other;
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
