package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * This gateway filter adds additional headers to the request
 */
@Order(41)
@Component
public class UpstreamHeaderFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(UpstreamHeaderFilter.class);

    public static final String X_PROXY = "X-PROXY";
    public static final String X_OAG_API_KEY = "X-OAG-ApiKey";
    public static final String X_OAG_STATUS = "X-OAG-Status";
    public static final String X_OAG_PROVIDER = "X-OAG-Provider";
    public static final String X_OAG_USER_PREFIX = "X-OAG-USER-";
    public static final String X_PROXY_VALUE = "OWASP Application Gateway";
    @Autowired
    MainConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        logTrace(log, exchange, "Execute ResponseHeaderFilter");

        var request = exchange.getRequest().mutate();

        request = request.header(X_PROXY, X_PROXY_VALUE);
        request = request.header(X_OAG_API_KEY, config.getDownstreamApiKey());

        var sessionOptional = (Optional<Session>) exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional != null && sessionOptional.isPresent()) {

            Session session = sessionOptional.get();
            HashMap<String, String> userMappings = session.getUserModel().getMappings();

            request = request.header(X_OAG_STATUS, SessionInformation.SESSION_STATE_AUTHENTICATED);
            request = request.header(X_OAG_PROVIDER, session.getProvider());

            for (var mapping : userMappings.entrySet()) {
                request = request.header(X_OAG_USER_PREFIX + mapping.getKey(), mapping.getValue());
            }

        } else {
            request = request.header(X_OAG_STATUS, SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return chain.filter(exchange.mutate().request(request.build()).build());
    }
}
