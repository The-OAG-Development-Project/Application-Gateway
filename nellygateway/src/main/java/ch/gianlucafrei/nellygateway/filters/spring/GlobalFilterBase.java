package ch.gianlucafrei.nellygateway.filters.spring;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public abstract class GlobalFilterBase implements WebFilter {

    protected ServerHttpRequest request;
    protected ServerHttpResponse response;
    protected ServerWebExchange exchange;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {

        this.exchange = serverWebExchange;
        this.request = serverWebExchange.getRequest();
        this.response = serverWebExchange.getResponse();

        filter();

        return webFilterChain.filter(serverWebExchange)
                .doOnSuccess(d -> onSuccess())
                .doOnError(t -> onError(t));
    }

    protected void onError(Throwable t) {
        
    }

    protected void filter() {
    }

    protected void onSuccess() {
    }
}
