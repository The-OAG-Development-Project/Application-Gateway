package org.owasp.oag.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static org.owasp.oag.filters.servlet.TraceContextFilter.TRACE_ID_CONTEXT_KEY;

/**
 * Provides method for contextual logging. The way this works is that the context is put into the mdc before the log statement
 * and directly removed after the log statement was executed.
 * <p>
 * How should I log?
 * <p>
 * Do you have access to the current request? For example in a filter or controller class.
 * -&gt; Use the logTrace(log, request, msg, params) method with the corresponding log level.
 * <p>
 * Otherwise just use the normal method of the logger object. For instance log.debug("Message").
 */
public class LoggingUtils {

    /**
     * Executes a trace log statement and wraps the request context around it.
     *
     * @param logger  The class logger
     * @param request The current request, may be null
     * @param msg     The log message
     * @param args    Additional log parameters
     */
    public static void logTrace(Logger logger, HttpServletRequest request, String msg, Object... args) {

        if (!logger.isTraceEnabled())
            return;

        if (request == null) {
            logger.trace(msg, args);
            return;
        }

        String context = (String) request.getAttribute(TRACE_ID_CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
            logger.trace(msg, args);
        }
    }

    /**
     * Executes a debug log statement and wraps the request context around it.
     *
     * @param logger  The class logger
     * @param request The current request, may be null
     * @param msg     The log message
     * @param args    Additional log parameters
     */
    public static void logDebug(Logger logger, HttpServletRequest request, String msg, Object... args) {

        if (!logger.isDebugEnabled())
            return;

        if (request == null) {
            logger.debug(msg, args);
            return;
        }

        String context = (String) request.getAttribute(TRACE_ID_CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
            logger.debug(msg, args);
        }
    }

    /**
     * Executes an info log statement and wraps the request context around it.
     *
     * @param logger  The class logger
     * @param request The current request, may be null
     * @param msg     The log message
     * @param args    Additional log parameters
     */
    public static void logInfo(Logger logger, HttpServletRequest request, String msg, Object... args) {

        if (!logger.isInfoEnabled())
            return;

        if (request == null) {
            logger.info(msg, args);
            return;
        }

        String context = (String) request.getAttribute(TRACE_ID_CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
            logger.info(msg, args);
        }
    }

    /**
     * Executes a warn log statement and wraps the request context around it.
     *
     * @param logger  The class logger
     * @param request The current request, may be null
     * @param msg     The log message
     * @param args    Additional log parameters
     */
    public static void logWarn(Logger logger, HttpServletRequest request, String msg, Object... args) {

        if (!logger.isWarnEnabled())
            return;

        if (request == null) {
            logger.warn(msg, args);
            return;
        }

        String context = (String) request.getAttribute(TRACE_ID_CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
            logger.warn(msg, args);
        }
    }

    /**
     * Executes an error log statement and wraps the request context around it.
     *
     * @param logger  The class logger
     * @param request The current request, may be null
     * @param msg     The log message
     * @param args    Additional log parameters
     */
    public static void logError(Logger logger, HttpServletRequest request, String msg, Object... args) {

        if (!logger.isErrorEnabled())
            return;

        if (request == null) {
            logger.error(msg, args);
            return;
        }

        String context = (String) request.getAttribute(TRACE_ID_CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
            logger.error(msg, args);
        }
    }
}
