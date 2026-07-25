package org.owasp.oag.utils;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Consumer;

import static org.owasp.oag.filters.spring.TraceContextFilter.TRACE_ID_CONTEXT_KEY;

/**
 * Provides method for contextual logging. The way this works is that the context is put into the mdc before the log statement
 * and directly removed after the log statement was executed.
 * <p>
 * How should I log?
 * <p>
 * Do you have access to the ServerWebExchange object? For example in a filter or controller class.
 * -> Use the logTrace(log, exchange, msg, params) method with the corresponding log level.
 * <p>
 * Otherwise just use the normal method of the logger object. For instance log.debug("Message).
 */
public class LoggingUtils {

    /**
     * Copies the context key from the subscription context to the mdc, executes the statement, and directly removes the mdc
     * should be used the following way: mono.doOnEach(logOnNext(s -> log.info("Log statement")))
     *
     * @param logStatement the log data
     * @param <T> The SignalClass
     * @return the next
     */
    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {

        return signal -> {
            if (!signal.isOnNext()) return;

            Optional<String> toPutInMdc = signal.getContextView().getOrEmpty(TRACE_ID_CONTEXT_KEY);

            toPutInMdc.ifPresentOrElse(tpim -> {
                        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, tpim)) {
                            logStatement.accept(signal.get());
                        }
                    },
                    () -> logStatement.accept(signal.get()));
        };
    }

    /**
     * Executes a trace log statement and wraps the request context around it.
     *
     * @param logger   The class logger
     * @param exchange The server exchange object
     * @param msg      The log message
     * @param args     Additional log parameters
     */
    public static void logTrace(Logger logger, ServerWebExchange exchange, String msg, Object... args) {

        if (!logger.isTraceEnabled())
            return;

        if (exchange == null) {
            logger.trace(msg, args);
        } else {

            String context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
            try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
                logger.trace(msg, args);
            }
        }
    }

    /**
     * Executes a debug log statement and wraps the request context around it.
     *
     * @param logger   The class logger
     * @param exchange The server exchange object
     * @param msg      The log message
     * @param args     Additional log parameters
     */
    public static void logDebug(Logger logger, ServerWebExchange exchange, String msg, Object... args) {

        if (!logger.isDebugEnabled())
            return;

        if (exchange == null) {
            logger.debug(msg, args);
        } else {
            String context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
            try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
                logger.debug(msg, args);
            }
        }
    }

    /**
     * Executes a info log statement and wraps the request context around it.
     *
     * @param logger   The class logger
     * @param exchange The server exchange object
     * @param msg      The log message
     * @param args     Additional log parameters
     */
    public static void logInfo(Logger logger, ServerWebExchange exchange, String msg, Object... args) {

        if (!logger.isInfoEnabled())
            return;

        if (exchange == null) {
            logger.info(msg, args);
        } else {
            String context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
            try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
                logger.info(msg, args);
            }
        }
    }

    /**
     * Executes a warn log statement and wraps the request context around it.
     *
     * @param logger   The class logger
     * @param exchange The server exchange object
     * @param msg      The log message
     * @param args     Additional log parameters
     */
    public static void logWarn(Logger logger, ServerWebExchange exchange, String msg, Object... args) {

        if (!logger.isWarnEnabled())
            return;

        if (exchange == null) {
            logger.warn(msg, args);
        } else {
            String context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
            try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
                logger.warn(msg, args);
            }
        }
    }

    /**
     * Executes a error log statement and wraps the request context around it.
     *
     * @param logger   The class logger
     * @param exchange The server exchange object
     * @param msg      The log message
     * @param args     Additional log parameters
     */
    public static void logError(Logger logger, ServerWebExchange exchange, String msg, Object... args) {

        if (!logger.isErrorEnabled())
            return;

        if (exchange == null) {
            logger.error(msg, args);
        } else {
            String context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
            try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, context)) {
                logger.error(msg, args);
            }
        }
    }
}
