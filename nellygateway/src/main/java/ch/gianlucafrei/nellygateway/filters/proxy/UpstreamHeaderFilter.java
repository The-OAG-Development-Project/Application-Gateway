package ch.gianlucafrei.nellygateway.filters.proxy;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
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

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logTrace;

/**
 * This gateway filter adds additional headers to the request
 */
@Order(41)
@Component
public class UpstreamHeaderFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(UpstreamHeaderFilter.class);

    public static final String X_PROXY = "X-PROXY";
    public static final String X_NELLY_API_KEY = "X-NELLY-ApiKey";
    public static final String X_NELLY_STATUS = "X-NELLY-Status";
    public static final String X_NELLY_PROVIDER = "X-NELLY-Provider";
    @Autowired
    NellyConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        logTrace(log, exchange, "Execute ResponseHeaderFilter");

        var request = exchange.getRequest().mutate();

        request = request.header(X_PROXY, "Nellygateway");
        request = request.header(X_NELLY_API_KEY, config.getNellyApiKey());

        var sessionOptional = (Optional<Session>) exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional != null && sessionOptional.isPresent()) {

            Session session = sessionOptional.get();
            HashMap<String, String> userMappings = session.getUserModel().getMappings();

            request = request.header(X_NELLY_STATUS, SessionInformation.SESSION_STATE_AUTHENTICATED);
            request = request.header(X_NELLY_PROVIDER, session.getProvider());

            for (var mapping : userMappings.entrySet()) {
                request = request.header("X-NELLY-USER-" + mapping.getKey(), mapping.getValue());
            }

        } else {
            request = request.header(X_NELLY_STATUS, SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return chain.filter(exchange.mutate().request(request.build()).build());
    }
}
