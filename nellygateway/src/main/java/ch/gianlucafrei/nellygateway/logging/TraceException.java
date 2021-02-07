package ch.gianlucafrei.nellygateway.logging;

/**
 * Exception thrown by the trace / correlation log subsystem in various situations.
 */
public class TraceException extends RuntimeException {
    public TraceException(String msg) {
        super(msg);
    }
}
