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

import java.util.HashMap;
import java.util.Optional;

@Order(4)
@Component
public class SessionRenewalFilter extends GlobalFilterBase {

    private static final Logger log = LoggerFactory.getLogger(SessionRenewalFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    NellyConfig config;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void filter() {

        Optional<Session> sessionOptional = (Optional<Session>) exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);
        var sessionBehavior = config.getSessionBehaviour();

        if (sessionOptional.isPresent() && sessionBehavior.getRenewWhenLessThan() > 0) // Feature which if renewal time is <= 0
        {
            var session = sessionOptional.get();
            long remainingTime = session.getRemainingTimeSeconds();
            int renewWhenLessThan = sessionBehavior.getRenewWhenLessThan();

            if (remainingTime < renewWhenLessThan)
                renewSession(session);
        }
    }

    private void renewSession(Session session) {

        log.debug("Start renewing session");
        var filterContext = new HashMap<String, Object>();
        filterContext.put("old-session", session);
        NellySessionFilter.runRenewSessionFilterChain(applicationContext, filterContext, response);
    }
}