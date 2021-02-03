package ch.gianlucafrei.nellygateway.filters.spring;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public abstract class GlobalFilterBase implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        filter(serverWebExchange);

        return webFilterChain.filter(serverWebExchange)
                .doOnSuccess(d -> onSuccess(serverWebExchange));
    }

    protected void onError(Throwable t, ServerWebExchange exchange) {

    }

    protected void filter(ServerWebExchange serverWebExchange) {
    }

    protected void onSuccess(ServerWebExchange exchange) {
    }
}
