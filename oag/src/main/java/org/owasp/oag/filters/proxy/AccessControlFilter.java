package org.owasp.oag.filters.proxy;

import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.filters.GatewayRouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

@Order(20)
@Component
public class AccessControlFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessControlFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute AccessControlFilter");

        var request = exchange.getRequest();
        var onlyAuthenticated = !routeContext.getRoute().isAllowAnonymous();
        var isAnonymous = routeContext.getSessionOptional().isEmpty();

        if (onlyAuthenticated && isAnonymous) {

            var response = exchange.getResponse();
            if(routeContext.getRoute().getAutoLogin() != null){
                redirectToLoginEndpoint(exchange, request, response, routeContext);
            }else {
                blockRequest(exchange, request, response, routeContext);
            }
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    private void redirectToLoginEndpoint(ServerWebExchange exchange, ServerHttpRequest request, ServerHttpResponse response, GatewayRouteContext routeContext) {

        Set<URI> requestUrls = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        Optional<URI> requestUrl = requestUrls.stream().findFirst();

        if(!requestUrl.isPresent())
            throw new ConsistencyException("Request Url is not known");

        String requestUrlEncoded = UriUtils.encodeQueryParam(requestUrl.get().toString(), "UTF-8");
        String loginEndpoint = String.format("%s/auth/%s/login?returnUrl=%s",
                config.getHostUri(), routeContext.getRoute().getAutoLogin(), requestUrlEncoded);

        logInfo(log, exchange, "Redirect unauthenticated request {} {} to login endpoint", request.getMethod(), routeContext.getRequestUri());
        response.getHeaders().add("Location", loginEndpoint);
        response.setStatusCode(HttpStatus.FOUND);
    }

    private void blockRequest(ServerWebExchange exchange, ServerHttpRequest request, ServerHttpResponse response, GatewayRouteContext routeContext) {

        logInfo(log, exchange, "Blocked unauthenticated request {} {}", request.getMethod(), routeContext.getRequestUri());
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
