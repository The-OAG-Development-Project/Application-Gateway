package org.owasp.oag.hooks.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Session hook that adds a session to the blacklist upon destruction.
 */
@Component
public class AddSessionToBlacklistHook implements SessionHook {

    private static final Logger log = LoggerFactory.getLogger(AddSessionToBlacklistHook.class);

    @Autowired
    private SessionBlacklist sessionBlacklist;

    /**
     * Renews the session. (Not implemented in this hook)
     *
     * @param filterContext The filter context.
     * @param response      The HTTP response.
     */
    @Override
    public void renewSession(Map<String, Object> filterContext, HttpServletResponse response) {

    }

    /**
     * Gets the order of this hook.
     *
     * @return The order of this hook.
     */
    @Override
    public int order() {
        return 3;
    }

    /**
     * Creates a session. (Not implemented in this hook)
     *
     * @param filterContext The filter context.
     * @param response      The HTTP response.
     */
    @Override
    public void createSession(Map<String, Object> filterContext, HttpServletResponse response) {

    }

    /**
     * Destroys a session by adding it to the blacklist.
     *
     * @param filterContext The filter context.
     * @param request       The current request.
     * @param response      The HTTP response.
     */
    @Override
    public void destroySession(Map<String, Object> filterContext, HttpServletRequest request,
                              HttpServletResponse response) {

        Optional<Session> sessionOptional = Session.REQUEST_SESSION.get();

        sessionOptional.ifPresent(session -> {
            sessionBlacklist.invalidateSession(session.getId(), session.getRemainingTimeSeconds());
            log.debug("Session {} invalidated.", session.getId());
        });
    }
}
