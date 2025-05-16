package org.owasp.oag.logging.w3ctrace;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.owasp.oag.exception.TraceException;
import org.owasp.oag.utils.SecureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Implements the W3C Trace Context specification. Holds the state required by W3CTraceContext.
 * See https://w3c.github.io/trace-context/
 */
public class W3cTraceContextState {

    private static final Logger log = LoggerFactory.getLogger(W3cTraceContextState.class);

    private static final SecureRandom secRandom = new SecureRandom();

    // Trace Info (traceparent)
    private final byte[] version = {0x0}; // we only support version 00
    private final byte[] traceId = new byte[16];
    private final byte[] parentId = new byte[8];
    private final byte[] flags = {0x1}; // we always log -> 01
    // the max acceptable traceparent size (usually it is just 3 * "-" but we accept " - " too.
    private final int traceparentMaxStringLength = (version.length + traceId.length + parentId.length + flags.length) * 2 + 3 * " - ".length();
    private volatile String cachedString = null; // we do not care when we calculate twice, but intermediate results are not wanted.
    // Secondary Trace Info (tracestate))
    private final String secondaryTraceInfoString;

    /**
     * Creates a new trace state with randomly generated trace ID and parent ID.
     * This constructor is used when starting a new trace.
     */
    public W3cTraceContextState() {
        secRandom.nextBytes(traceId);
        secRandom.nextBytes(parentId);
        secondaryTraceInfoString = null;
    }

    /**
     * Creates a new trace state based on the passed in existing trace id data and logs as appropriate.
     *
     * @param traceparentString the traceparent
     * @param tracestateString  the tracestate
     * @param maxTraceStateLength the maximum length allowed for the tracestate string
     * @throws TraceException when the passed in data is invalid.
     */
    public W3cTraceContextState(String traceparentString, String tracestateString, int maxTraceStateLength) {

        // traceparent part
        if (traceparentString == null || traceparentString.length() == 0) {
            log.info("No primary trace id provided, will not take over any data.");
            throw new TraceException("traceparent not provided.");
        }

        if (traceparentString.length() > traceparentMaxStringLength) {
            log.info("Bad trace format, traceparent is to long. Ignoring incoming trace id {}.", SecureEncoder.encodeStringForLog(traceparentString, traceparentMaxStringLength));
            throw new TraceException("traceparent to long.");
        }

        StringTokenizer parts = new StringTokenizer(traceparentString, "-", false);
        if (parts.countTokens() != 4) {
            log.info("Bad trace format, traceparent is not specification compliant (see https://w3c.github.io/trace-context/). Ignoring incoming trace id {}.", SecureEncoder.encodeStringForLog(traceparentString, traceparentMaxStringLength));
            throw new TraceException("traceparent format wrong.");
        }

        try {
            convertTokenToByteArray(traceparentString, parts, version, "version");
            convertTokenToByteArray(traceparentString, parts, traceId, "traceId");
            convertTokenToByteArray(traceparentString, parts, parentId, "parentId");
            convertTokenToByteArray(traceparentString, parts, flags, "flags");
        } catch (Exception e) {
            log.info("Bad trace format, traceparent is not specification compliant (see https://w3c.github.io/trace-context/). Error was {}. Ignoring incoming trace id {}.", e.getMessage(), SecureEncoder.encodeStringForLog(traceparentString, traceparentMaxStringLength));
            throw new TraceException("traceparent format wrong");
        }

        if (allZero(traceId) || allZero(parentId)) {
            log.info("Bad trace format, traceparent is not specification compliant (see https://w3c.github.io/trace-context/). TraceId or ParentId are all 0 which is not allowed. Ignoring incoming trace id {}.", SecureEncoder.encodeStringForLog(traceparentString, traceparentMaxStringLength));
            throw new TraceException("traceparent format wrong");
        }

        // tracestate part (note: size that should be supported according to spec is 512, that is why we limit log to this when 0 is set accidentially.
        final int maxTraceStateSize = (maxTraceStateLength != 0) ? maxTraceStateLength : 512;
        final int maxLogSize = 64;
        String tmpSecondaryTraceInfo = null;
        try {
            if (tracestateString == null || tracestateString.length() == 0) {
                log.debug("No secondary trace info provided.");
            } else {
                if (maxTraceStateSize < tracestateString.length()) {
                    tracestateString = tracestateString.substring(0, maxTraceStateSize);
                }
                // tracestate must be key=value pairs (list of these) if we have truncated them, we must make sure it ends with a clean pair
                if (StringUtils.countMatches(tracestateString, "=") <= StringUtils.countMatches(tracestateString, ",")) {
                    // no well formated tracestate
                    // maybe it was truncated when reading, try to fix that by falling back to the last ,
                    if (tracestateString.lastIndexOf(",") == -1) {
                        log.info("secondary trace info was invalid. Ignoring value provided: {}", SecureEncoder.encodeStringForLog(tracestateString, maxLogSize));
                    } else {
                        String fix = tracestateString.substring(0, tracestateString.lastIndexOf(","));
                        if (StringUtils.countMatches(fix, "=") <= StringUtils.countMatches(fix, ",")) {
                            log.info("secondary trace info was invalid. Ignoring value provided: {}", SecureEncoder.encodeStringForLog(tracestateString, maxLogSize));
                        } else {
                            tmpSecondaryTraceInfo = fix;
                        }
                    }
                } else {
                    tmpSecondaryTraceInfo = tracestateString;
                }

                if (tmpSecondaryTraceInfo != null && StringUtils.countMatches(tmpSecondaryTraceInfo, "=") != StringUtils.countMatches(tmpSecondaryTraceInfo, ",") + 1) {
                    log.info("secondary trace info was invalid. Ignoring value provided: {}", SecureEncoder.encodeStringForLog(tracestateString, maxLogSize));
                    tmpSecondaryTraceInfo = null;
                }
            }
        } catch (Exception e) {
            log.info("Bad trace format, tracestate is not specification compliant (see https://w3c.github.io/trace-context/). Error was {}. Ignoring incoming tracestate {}. Just using traceparent part.", e.getMessage(), SecureEncoder.encodeStringForLog(tracestateString, maxLogSize));
        }

        if (tmpSecondaryTraceInfo != null) {
            log.debug("secondary trace info looks ok, taking it over.");
        }

        secondaryTraceInfoString = tmpSecondaryTraceInfo;
    }

    private boolean allZero(byte[] checkForNotNull) {
        for (byte b : checkForNotNull) {
            if (b != (byte) 0) {
                return false;
            }
        }
        return true;
    }

    private void convertTokenToByteArray(String traceparentString, StringTokenizer parts, final byte[] tracePart, String tracePartString) throws DecoderException {
        String tmp = parts.nextToken().trim();
        byte[] bTmp = Hex.decodeHex(tmp);
        if (bTmp.length != tracePart.length) {
            log.info("Bad trace format, traceparent is not specification compliant (see https://w3c.github.io/trace-context/). {} size is wrong. Ignoring incoming trace id {}.", tracePartString, SecureEncoder.encodeStringForLog(traceparentString, traceparentMaxStringLength + 10));
            throw new TraceException("traceparent format wrong: " + tracePartString);
        }
        System.arraycopy(bTmp, 0, tracePart, 0, tracePart.length);
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
