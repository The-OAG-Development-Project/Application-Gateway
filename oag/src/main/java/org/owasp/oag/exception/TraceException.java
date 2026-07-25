package org.owasp.oag.exception;

/**
 * Exception thrown by the trace / correlation log subsystem in various situations.
 * <p>
 * This signals that trace information supplied by a caller cannot be used, which is an ordinary
 * consequence of untrusted input rather than a fault of the gateway. It therefore logs at debug
 * level: every throw site already writes an explanatory info statement with the offending value
 * encoded for logging, so a caller sending malformed trace headers must not additionally be able
 * to inflate the log at warning level.
 */
public class TraceException extends ApplicationException {
    /**
     * Constructs a new TraceException with the specified error message.
     *
     * @param msg The error message
     */
    public TraceException(String msg) {
        super(msg, null, false);
    }
}
