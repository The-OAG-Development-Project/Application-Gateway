package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.session.NellySessionFilter;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.session.Session;
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

import java.util.HashMap;
import java.util.Optional;

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logDebug;
import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.logTrace;


@Order(50)
@Component
public class SessionRenewalFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionRenewalFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    NellyConfig config;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        logTrace(log, exchange, "Execute SessionRenewalFilter");

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);
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

        logDebug(log, exchange,"Start renewing session");
        var filterContext = new HashMap<String, Object>();
        filterContext.put("old-session", session);
        NellySessionFilter.runRenewSessionFilterChain(applicationContext, filterContext, exchange.getResponse());
    }
}