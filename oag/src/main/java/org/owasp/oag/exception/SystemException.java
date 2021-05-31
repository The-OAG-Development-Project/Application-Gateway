package org.owasp.oag.exception;

/**
 * Usage: Throw this exception whenever the code is fine but the environemnt is bad.
 * For example bad configuration, timeout, hostname not known, external system not available, etc.
 * User Reaction: Wait and try later.
 * Responsible to fix: Operations.
 * Log level: Usually Warning, max Error, min Info.
 * May create subclasses do distinguish errors and provide more details.
 * If creating subclasses create them by error/problem type, NOT by external system or similar.
 */
public class SystemException extends AbstractException {

    /**
     * creates a new exception logged at default Warning level
     *
     * @param message the message to log
     */
    public SystemException(String message) {
        this(message, null);
    }

    /**
     * creates a new exception logged at default Warning level
     *
     * @param message         the message to log
     * @param parentException the parent exception if available.
     */
    public SystemException(String message, Throwable parentException) {
        super(message, parentException, ExceptionLogLevel.WARNING);
    }

    /**
     * creates a new exception logged at higher Error level or lower Info level
     *
     * @param message          the message to log
     * @param elevatedLogLevel set to true to log with Error level, else logs with Info Level
     */
    public SystemException(String message, boolean elevatedLogLevel) {
        this(message, null, elevatedLogLevel);
    }

    /**
     * creates a new exception logged at higher Error level or lower Info level
     *
     * @param message          the message to log
     * @param parentException  the parent exception if available.
     * @param elevatedLogLevel set to true to log with Error level, else logs with Info Level
     */
    public SystemException(String message, Throwable parentException, boolean elevatedLogLevel) {
        super(message, parentException, (elevatedLogLevel ? ExceptionLogLevel.ERROR : ExceptionLogLevel.INFO));
    }
}
