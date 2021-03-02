package org.owasp.oag.logging;

/**
 * Exception thrown by the trace / correlation log subsystem in various situations.
 */
public class TraceException extends RuntimeException {
    public TraceException(String msg) {
        super(msg);
    }
}
