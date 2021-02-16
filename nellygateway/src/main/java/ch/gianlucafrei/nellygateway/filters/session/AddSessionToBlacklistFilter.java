package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.services.blacklist.SessionBlacklist;
import ch.gianlucafrei.nellygateway.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.Optional;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logDebug;
import static ch.gianlucafrei.nellygateway.utils.ReactiveUtils.subscribeAsynchronously;

@Component
public class AddSessionToBlacklistFilter implements NellySessionFilter {

    private static final Logger log = LoggerFactory.getLogger(AddSessionToBlacklistFilter.class);

    @Autowired
    SessionBlacklist sessionBlacklist;

    @Override
    public void renewSession(Map<String, Object> filterContext, ServerHttpResponse response) {

    }

    @Override
    public int order() {
        return 3;
    }

    @Override
    public void createSession(Map<String, Object> filterContext, ServerHttpResponse response) {

    }

    @Override
    public void destroySession(Map<String, Object> filterContext, ServerWebExchange exchange) {

        Optional<Session> sessionOptional = ExtractAuthenticationFilter.extractSessionFromExchange(exchange);

        // Session will be stored in asynchronously in session blacklist
        sessionOptional.ifPresent(session -> {

            subscribeAsynchronously(
                    sessionBlacklist.invalidateSession(session.getId(), session.getRemainingTimeSeconds())
                    .doOnSuccess((unused) -> logDebug(log, exchange,"Session {} invalidated", session.getId()))
                    ,exchange);
        });
    }
}
