package ch.gianlucafrei.nellygateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

public class ReactiveUtils {

    private static final Logger log = LoggerFactory.getLogger(ReactiveUtils.class);

    public static <T> Mono<T> runBlockingProcedure(Callable<T> blockingFunction) {

        return Mono.fromCallable(blockingFunction)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
