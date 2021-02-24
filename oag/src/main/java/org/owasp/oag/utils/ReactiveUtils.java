package org.owasp.oag.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

import static org.owasp.oag.filters.spring.TraceContextFilter.CONTEXT_KEY;
import static org.owasp.oag.utils.LoggingUtils.wrapMdc;

public class ReactiveUtils {

    private static final Logger log = LoggerFactory.getLogger(ReactiveUtils.class);

    /**
     * Returns a mono that runs the blocking method on another thread
     * @param blockingFunction
     * @param <T>
     * @return
     */
    public static <T> Mono<T> runBlockingProcedure(Callable<T> blockingFunction) {

        return wrapMdc(blockingFunction)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns a mono that runs the blocking method on another thread
     * @param blockingFunction
     * @return
     */
    public static Mono<Void> runBlockingProcedure(Runnable blockingFunction) {

        return wrapMdc(blockingFunction)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public static <T> Mono<T> copyContext(Mono<T> mono, ServerWebExchange exchange){

        var context = exchange.getAttribute(CONTEXT_KEY);
        return mono.contextWrite(c -> c.put(CONTEXT_KEY, context));
    }

    public static void subscribeAsynchronously(Mono<?> mono, ServerWebExchange exchange){

        copyContext(mono, exchange).subscribe();
    }


}
