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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Order(4)
@Component
public class SessionRenewalFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SessionRenewalFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    NellyConfig config;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;


        Optional<Session> sessionOptional = (Optional<Session>) req.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);
        var sessionBehavior = config.getSessionBehaviour();

        if (sessionOptional.isPresent() && sessionBehavior.getRenewWhenLessThan() > 0) // Feature which if renewal time is <= 0
        {
            var session = sessionOptional.get();
            long remainingTime = session.getRemainingTimeSeconds();
            int renewWhenLessThan = sessionBehavior.getRenewWhenLessThan();
            if (remainingTime < renewWhenLessThan)
                renewSession(session, res);
        }

        // Process other filters
        chain.doFilter(request, res);
    }

    private void renewSession(Session session, HttpServletResponse res) {

        log.debug("Start renewing session");
        var filterContext = new HashMap<String, Object>();
        filterContext.put("old-session", session);
        NellySessionFilter.runRenewSessionFilterChain(applicationContext, filterContext, res);
    }
}