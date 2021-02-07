package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

// TODO add flag to ignore this filter
@Order(2)
@Component
public class HttpRedirectFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpRedirectFilter.class);

    @Autowired
    private NellyConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        if (config.isHttpsHost()) {
            // We do the request only if we are on https

            if (isInsecureRequest(request)) {
                sendHttpsRedirectResponse(exchange);
                log.debug("Redirected insecure request to {}", request.getURI().getPath());
                return response.setComplete();
            }
        }
        return chain.filter(exchange);
    }

    public boolean isInsecureRequest(ServerHttpRequest request) {

        // If we are on http only https redirection is irrelevant
        if (config.isHttpsHost())
            return false;

        // Check if request was forwarded
        if (request.getHeaders().getFirst("X-Forwarded-For") != null) {

            // if X-Forwarded-Proto: https we threat the request as secure
            return !"https".equals(request.getHeaders().getFirst("X-Forwarded-Proto"));
        }

        String scheme = request.getURI().getScheme();
        if ("http".equals(scheme))
            return true;

        if ("https".equals(scheme))
            return false;

        // Fallback if everything else fails (don't redirect)
        log.debug("Unsecure if request if https");
        return false;
    }

    public void sendHttpsRedirectResponse(ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        var requestUri = request.getURI();
        var queryString = requestUri.getRawQuery();

        var redirectLocation = config.getHostUri() + requestUri.getPath() + queryString
                + (queryString != null ? "?" + queryString : "");

        response.getHeaders().add("Location", redirectLocation);
        response.setRawStatusCode(302);
    }
}