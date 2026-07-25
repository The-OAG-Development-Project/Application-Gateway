package org.owasp.oag.exception;

import java.util.UUID;

/**
 * Base class for all OAG exceptions. Just used as a means to share code. Do not cast or catch on this exception.
 * There are 3 main types of OAG Exceptions with distinct meaning amd purpose:
 * - ApplicationExceptions
 * - SystemExceptions
 * - ConsistencyExceptions
 * <p>
 * Implements functionality to create a unique exception ID per creation.
 * This is to identify the request (correlationID/traceId) that lead to the exception and support debugging.
 */
public class AbstractException extends RuntimeException {

    /**
     * Unique identifier for this exception instance.
     * Generated as a random UUID to help with tracking and debugging.
     */
    private final String exceptionId = UUID.randomUUID().toString();

    /**
     * Creates a new AbstractException with the given message, parent exception, and log level.
     *
     * @param message         The message describing the exception.
     * @param parentException The parent exception if available.
     * @param forceLogLevel   The log level to force for this exception.
     */
    protected AbstractException(String message, Throwable parentException, ExceptionLogLevel forceLogLevel) {
        super(message, parentException);
        validateLogLevel(forceLogLevel);
    }

    /**
     * Validates that the given log level is one of the known {@link ExceptionLogLevel} values.
     *
     * @param forceLogLevel The log level to validate.
     */
    private void validateLogLevel(ExceptionLogLevel forceLogLevel) {
        switch (forceLogLevel) {
            case TRACE, DEBUG, INFO, WARNING, ERROR -> {
                // valid level, nothing further to do
            }
            default -> throw new ConsistencyException("Unknown log level " + forceLogLevel, null);
        }
    }

    @Override
    public String getMessage() {
        return "ExId: " + exceptionId + ": " + super.getMessage();
    }

    /**
     * @return the unique exception id of this instance, generated during creation.
     */
    public String getExceptionId() {
        return exceptionId;
    }
}
