package org.owasp.oag.filters.spring;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.hooks.session.SessionHookChain;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Order(50)
@Component
public class SessionRenewalFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionRenewalFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    MainConfig config;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SessionHookChain sessionHookChain;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        LoggingUtils.logTrace(log, exchange, "Execute SessionRenewalFilter");

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);
        var sessionBehavior = config.getSessionBehaviour();

        if (sessionOptional.isPresent() && sessionBehavior.getRenewWhenLessThan() > 0) // Feature switch if renewal time is <= 0
        {
            var session = sessionOptional.get();
            long remainingTime = session.getRemainingTimeSeconds();
            int renewWhenLessThan = sessionBehavior.getRenewWhenLessThan();

            if (remainingTime < renewWhenLessThan)
                renewSession(session, exchange);
        }

        return chain.filter(exchange);
    }

    private void renewSession(Session session, ServerWebExchange exchange) {

        LoggingUtils.logDebug(log, exchange,"Start renewing session");
        sessionHookChain.renewSession(session, exchange.getResponse());
    }
}