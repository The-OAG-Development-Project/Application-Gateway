package org.owasp.oag.exception;

/**
 * Usage: Throw this exception whenever the code is fine but the client provided bad data or the semantics of the call are not valid.
 * User Reaction: Fix input and try again.
 * Responsible to fix: Caller/Client.
 * Log level: Usually info, max Warning.
 * May create subclasses do distinguish errors and provide more details.
 */
public class ApplicationException extends AbstractException {

    /**
     * creates a new exception logged at default Info level
     *
     * @param message the message to log
     */
    public ApplicationException(String message) {
        this(message, null);
    }

    /**
     * creates a new exception logged at default Info level
     *
     * @param message         the message to log
     * @param parentException the parent exception if available.
     */
    public ApplicationException(String message, Throwable parentException) {
        super(message, parentException, ExceptionLogLevel.INFO);
    }

    /**
     * creates a new exception logged at higher warn level level
     *
     * @param message          the message to log
     * @param elevatedLogLevel set to true to log with WARN level, else logs with Debug Level
     */
    public ApplicationException(String message, boolean elevatedLogLevel) {
        this(message, null, elevatedLogLevel);
    }

    /**
     * creates a new exception logged at higher warn level level
     *
     * @param message          the message to log
     * @param parentException  the parent exception if available.
     * @param elevatedLogLevel set to true to log with WARN level, else logs with Debug Level
     */
    public ApplicationException(String message, Throwable parentException, boolean elevatedLogLevel) {
        super(message, parentException, (elevatedLogLevel ? ExceptionLogLevel.WARNING : ExceptionLogLevel.DEBUG));
    }
}
