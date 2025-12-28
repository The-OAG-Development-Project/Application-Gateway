package org.owasp.oag.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

import static org.owasp.oag.filters.spring.TraceContextFilter.TRACE_ID_CONTEXT_KEY;
import static org.owasp.oag.utils.LoggingUtils.wrapMdc;

/**
 * Utility class for working with reactive programming patterns.
 * Provides methods for running blocking operations on separate threads,
 * copying context between reactive streams, and managing asynchronous subscriptions.
 */
public class ReactiveUtils {

    private static final Logger log = LoggerFactory.getLogger(ReactiveUtils.class);

    /**
     * Returns a mono that runs the blocking method on another thread.
     * This method is useful for executing blocking operations without blocking the reactive event loop.
     * 
     * @param blockingFunction The callable function containing blocking code to execute
     * @param <T> The type of result returned by the blocking function
     * @return A Mono that will emit the result of the blocking function when complete
     */
    public static <T> Mono<T> runBlockingProcedure(Callable<T> blockingFunction) {

        return wrapMdc(blockingFunction)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns a mono that runs the blocking method on another thread.
     * This method is useful for executing blocking operations that don't return a value,
     * without blocking the reactive event loop.
     * 
     * @param blockingFunction The runnable containing blocking code to execute
     * @return A Mono&lt;Void&gt; that completes when the blocking operation is finished
     */
    public static Mono<Void> runBlockingProcedure(Runnable blockingFunction) {

        return wrapMdc(blockingFunction)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * Copies the trace context from a ServerWebExchange to a Mono.
     * This ensures that trace information is propagated through the reactive chain.
     * 
     * @param <T> The type of the Mono
     * @param mono The Mono to copy the context to
     * @param exchange The ServerWebExchange containing the trace context
     * @return A new Mono with the trace context added
     */
    public static <T> Mono<T> copyContext(Mono<T> mono, ServerWebExchange exchange){

        var context = exchange.getAttribute(TRACE_ID_CONTEXT_KEY);
        return mono.contextWrite(c -> c.put(TRACE_ID_CONTEXT_KEY, context));
    }

    /**
     * Subscribes to a Mono asynchronously, ensuring that trace context is propagated.
     * This method is useful for "fire and forget" operations where the result is not needed.
     * 
     * @param mono The Mono to subscribe to
     * @param exchange The ServerWebExchange containing the trace context
     */
    public static void subscribeAsynchronously(Mono<?> mono, ServerWebExchange exchange){

        copyContext(mono, exchange).subscribe();
    }


}
