package ch.gianlucafrei.nellygateway.utils;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static ch.gianlucafrei.nellygateway.filters.spring.TraceContextFilter.CONTEXT_KEY;

/**
 * Provides method for contextual logging. The way this works is that the context is put into the mdc before the log statement
 * and directly removed after the log statement was executed.
 *
 * How should I log?
 *
 * Do you have access to the ServerWebExchange object? For example in a filter or controller class.
 * -> Use the logTrace(log, exchange, msg, params) method with the corresponding log level.
 *
 * Does the method return a Mono object?
 * -> Use the logOnNext() or contextual() method.
 * -> logOnNext:  mono.doOnEach(logOnNext(s -> log.info("Log statement")))
 * -> contextual: contextual(() -> log.trace("Log statement"))
 *
 * Otherwise just use the normal method of the logger object. For instance log.debug("Message). The caller of the method
 * must make sure that the code is wrapped with the wrapMdc() methods.
 */
public class LoggingUtils {

    /**
     * Copies the context key from the subscription context to the mdc, executes the statement, and directly removes the mdc
     * should be used the following way: mono.doOnEach(logOnNext(s -> log.info("Log statement")))
     *
     * @param logStatement
     * @param <T>
     * @return
     */
    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {

        return signal -> {
            if (!signal.isOnNext()) return;

            Optional<String> toPutInMdc = signal.getContextView().getOrEmpty(CONTEXT_KEY);

            toPutInMdc.ifPresentOrElse(tpim -> {
                        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, tpim)) {
                            logStatement.accept(signal.get());
                        }
                    },
                    () -> logStatement.accept(signal.get()));
        };
    }

    /**
     * Wraps the mdc around a statement and returns a mono that executes it.
     * @param logStatement
     * @return
     */
    public static Mono<Void> contextual(Runnable logStatement){

        return wrapMdc(logStatement).then();
    }

    /**
     * Converts a callable to a mono and wraps it with the MDC context
     * Example: wrapMdc(() -> blockingFunction(id)).then(....)
     *
     * @param method
     * @param <T>
     * @return
     */
    public static <T> Mono<T> wrapMdc(Callable<T> method) {

        return Mono.deferContextual(ctx -> {

            Optional<String> toPutInMdc = ctx.getOrEmpty(CONTEXT_KEY);
            if(toPutInMdc.isEmpty()) {
                return Mono.fromCallable(method);
            }
            else {
                Callable<T> wrapped = () -> {
                    try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, toPutInMdc.get())) {
                        return method.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                return Mono.fromCallable(wrapped);
            }
        });
    }

    /**
     * Converts a runnable to a mono and wraps it with the MDC context
     * Example: wrapMdc(() -> blockingFunction(id)).then(....)
     * @param method
     * @return
     */
    public static Mono<Void> wrapMdc(Runnable method) {

        return Mono.deferContextual(ctx -> {

            Optional<String> toPutInMdc = ctx.getOrEmpty(CONTEXT_KEY);
            if(toPutInMdc.isEmpty()) {
                method.run();
                return Mono.empty();
            }
            else{
                Runnable wrapped = () -> {
                    try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, toPutInMdc.get())) {
                        method.run();
                    }
                };
                return Mono.fromRunnable(wrapped);
            }
        });
    }


    /**
     * Executes a trace log statement and wraps the request context around it.
     * @param logger The class logger
     * @param exchange The server exchange object
     * @param msg The log message
     * @param args Additional log parameters
     */
    public static void logTrace(Logger logger, ServerWebExchange exchange, String msg, Object... args){

        if(! logger.isTraceEnabled())
            return;

        String context = exchange.getAttribute(CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, context)){
            logger.trace(msg, args);
        }
    }

    /**
     * Executes a debug log statement and wraps the request context around it.
     * @param logger The class logger
     * @param exchange The server exchange object
     * @param msg The log message
     * @param args Additional log parameters
     */
    public static void logDebug(Logger logger, ServerWebExchange exchange, String msg, Object... args){

        if(! logger.isDebugEnabled())
            return;

        String context = exchange.getAttribute(CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, context)){
            logger.debug(msg, args);
        }
    }

    /**
     * Executes a info log statement and wraps the request context around it.
     * @param logger The class logger
     * @param exchange The server exchange object
     * @param msg The log message
     * @param args Additional log parameters
     */
    public static void logInfo(Logger logger, ServerWebExchange exchange, String msg, Object... args){

        if(! logger.isInfoEnabled())
            return;

        String context = exchange.getAttribute(CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, context)){
            logger.info(msg, args);
        }
    }

    /**
     * Executes a warn log statement and wraps the request context around it.
     * @param logger The class logger
     * @param exchange The server exchange object
     * @param msg The log message
     * @param args Additional log parameters
     */
    public static void logWarn(Logger logger, ServerWebExchange exchange, String msg, Object... args){

        if(! logger.isWarnEnabled())
            return;

        String context = exchange.getAttribute(CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, context)){
            logger.warn(msg, args);
        }
    }

    /**
     * Executes a error log statement and wraps the request context around it.
     * @param logger The class logger
     * @param exchange The server exchange object
     * @param msg The log message
     * @param args Additional log parameters
     */
    public static void logError(Logger logger, ServerWebExchange exchange, String msg, Object... args){

        if(! logger.isErrorEnabled())
            return;

        String context = exchange.getAttribute(CONTEXT_KEY);
        try (MDC.MDCCloseable cMdc = MDC.putCloseable(CONTEXT_KEY, context)){
            logger.error(msg, args);
        }
    }
}
