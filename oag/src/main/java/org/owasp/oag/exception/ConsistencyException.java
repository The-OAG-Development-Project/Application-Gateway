package org.owasp.oag.exception;

/**
 * Usage: Throw this exception whenever there is an error in the code or you reached a "should never happen" situation.
 * For example instead of comments "should never happen", in default switch cases where all enums are already tried out.
 * User Reaction: Do not retry, needs to be fixed in code.
 * Responsible to fix: Developer.
 * Log level: Usually Error, min Warn.
 * Usually do not create sub-classes of this.
 */
public final class ConsistencyException extends AbstractException {


    /**
     * creates a new exception logged at default error level
     *
     * @param message the message to log
     */
    public ConsistencyException(String message) {
        this(message, null);
    }

    /**
     * creates a new exception logged at default error level
     *
     * @param message         the message to log
     * @param parentException the parent exception if available.
     */
    public ConsistencyException(String message, Throwable parentException) {
        super(message, parentException, ExceptionLogLevel.ERROR);
    }

    /**
     * creates a new exception logged at lower level if lowerLevel is true. Else logs at default level error.
     *
     * @param message    the message to log
     * @param lowerLevel set to true to log with Warning level, setting to false logs with Error Level
     */
    public ConsistencyException(String message, boolean lowerLevel) {
        this(message, null, lowerLevel);
    }

    /**
     * creates a new exception logged at lower level if lowerLevel is true. Else logs at default level error.
     *
     * @param message         the message to log
     * @param parentException the parent exception if available.
     * @param lowerLevel      set to true to log with Warning level, setting to false logs with default Error Level
     */
    public ConsistencyException(String message, Throwable parentException, boolean lowerLevel) {
        super(message, parentException, (lowerLevel ? ExceptionLogLevel.WARNING : ExceptionLogLevel.ERROR));
    }
}
