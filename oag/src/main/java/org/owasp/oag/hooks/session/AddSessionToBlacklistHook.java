package org.owasp.oag.hooks.session;

import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.ReactiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logDebug;

@Component
public class AddSessionToBlacklistHook implements SessionHook {

    private static final Logger log = LoggerFactory.getLogger(AddSessionToBlacklistHook.class);

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

            ReactiveUtils.subscribeAsynchronously(
                    sessionBlacklist.invalidateSession(session.getId(), session.getRemainingTimeSeconds())
                    .doOnSuccess((unused) -> logDebug(log, exchange,"Session {} invalidated", session.getId()))
                    ,exchange);
        });
    }
}
