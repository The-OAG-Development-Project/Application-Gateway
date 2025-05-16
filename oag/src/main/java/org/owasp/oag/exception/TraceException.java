package org.owasp.oag.exception;

/**
 * Exception thrown by the trace / correlation log subsystem in various situations.
 */
public class TraceException extends SystemException {
    /**
     * Constructs a new TraceException with the specified error message.
     *
     * @param msg The error message
     */
    public TraceException(String msg) {
        super(msg);
    }
}
