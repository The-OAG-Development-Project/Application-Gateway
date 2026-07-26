package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.hooks.session.SessionHookChain;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that automatically renews sessions that are about to expire.
 * Checks the remaining time of the current session and renews it if it's below the configured threshold.
 */
@Order(50)
@Component
public class SessionRenewalFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionRenewalFilter.class);

    @Autowired
    private MainConfig config;

    @Autowired
    private SessionHookChain sessionHookChain;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        LoggingUtils.logTrace(log, request, "Execute SessionRenewalFilter");

        Optional<Session> sessionOptional = ExtractAuthenticationFilter.extractSessionFromRequest(request);
        var sessionBehavior = config.getSessionBehaviour();

        if (sessionOptional.isPresent() && sessionBehavior.getRenewWhenLessThan() > 0) // Feature switch if renewal time is <= 0
        {
            var session = sessionOptional.get();
            long remainingTime = session.getRemainingTimeSeconds();
            int renewWhenLessThan = sessionBehavior.getRenewWhenLessThan();

            if (remainingTime < renewWhenLessThan)
                renewSession(session, request, response);
        }

        chain.doFilter(request, response);
    }

    private void renewSession(Session session, HttpServletRequest request, HttpServletResponse response) {

        LoggingUtils.logDebug(log, request, "Start renewing session");
        sessionHookChain.renewSession(session, response);
    }
}
