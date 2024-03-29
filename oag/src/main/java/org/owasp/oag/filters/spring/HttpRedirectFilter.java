package org.owasp.oag.filters.spring;

import org.jetbrains.annotations.NotNull;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.utils.LoggingUtils;
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

@Order(30)
@Component
public class HttpRedirectFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpRedirectFilter.class);

    @Autowired
    private MainConfig config;

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {

        LoggingUtils.logTrace(log, exchange, "Execute HttpRedirectFilter");

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