package org.owasp.oag.exception;

/**
 * Exception thrown by the trace / correlation log subsystem in various situations.
 */
public class TraceException extends SystemException {
    public TraceException(String msg) {
        super(msg, null);
    }
}
