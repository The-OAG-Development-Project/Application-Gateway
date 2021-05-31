package org.owasp.oag.exception;

import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Base class for all OAG exceptions. Just used as a means to share code. Do not cast or catch on this exception.
 * There are 3 main types of OAG Exceptions with distinct meaning amd purpose:
 * - ApplicationExceptions
 * - SystemExceptions
 * - ConsistencyExceptions
 * <p>
 * Implements functionality to create a unique exception ID per creation and writes a log statement.
 * This is to identify the request (correlationID/traceId) that lead to the exception and support debugging.
 */
public class AbstractException extends RuntimeException {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String exceptionId = UUID.randomUUID().toString();

    protected AbstractException(String message, Throwable parentException, ExceptionLogLevel forceLogLevel) {
        super(message, parentException);
        log("ExId: " + exceptionId + ": " + message, forceLogLevel);
    }

    private void log(String message, ExceptionLogLevel forceLogLevel) {
        switch (forceLogLevel) {
            case TRACE:
                LoggingUtils.contextual(() -> log.trace(message, getCause()));
                break;
            case DEBUG:
                LoggingUtils.contextual(() -> log.debug(message, getCause()));
                break;
            case INFO:
                LoggingUtils.contextual(() -> log.info(message, getCause()));
                break;
            case WARNING:
                LoggingUtils.contextual(() -> log.warn(message, getCause()));
                break;
            case ERROR:
                LoggingUtils.contextual(() -> log.error(message, getCause()));
                break;
            default:
                LoggingUtils.contextual(() -> log.info("Original exception with invalid log level: " + message, getCause()));
                throw new ConsistencyException("Unknown log level " + forceLogLevel, null);
        }

    }

    @Override
    public String getMessage() {
        return "ExId: " + exceptionId + ": " + super.getMessage();
    }

    /**
     * @return the unique exception id of this instance, as written to the log during creation.
     */
    public String getExceptionId() {
        return exceptionId;
    }
}
