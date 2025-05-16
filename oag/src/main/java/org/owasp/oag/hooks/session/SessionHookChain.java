package org.owasp.oag.hooks.session;

import org.owasp.oag.session.Session;
import org.owasp.oag.session.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages and executes the chain of session hooks.
 * This component is responsible for collecting all session hooks in the application
 * and executing them in the correct order during session lifecycle events such as
 * creation, renewal, and destruction.
 */
@Component
public class SessionHookChain {

    private final List<SessionHook> hooks;

    /**
     * Creates a new session hook chain by collecting all session hooks from the application context.
     *
     * @param context The Spring application context from which to collect session hooks
     */
    public SessionHookChain(@Autowired ApplicationContext context){
        hooks = getSessionHooks(context);
    }

    /**
     * Creates a new session by executing all session creation hooks.
     * Each hook in the chain will have the opportunity to contribute to or modify
     * the session creation process.
     *
     * @param providerKey The authentication provider key
     * @param model The user model containing user information
     * @param response The HTTP response where cookies or headers can be added
     */
    public void createSession(String providerKey, UserModel model, ServerHttpResponse response) {
        var filterContext = new HashMap<String, Object>();
        filterContext.put("providerKey", providerKey);
        filterContext.put("userModel", model);

        hooks.forEach(h -> h.createSession(filterContext, response));
    }

    /**
     * Renews an existing session by executing all session renewal hooks.
     * Each hook in the chain will have the opportunity to contribute to or modify
     * the session renewal process.
     *
     * @param oldSession The existing session to be renewed
     * @param response The HTTP response where cookies or headers can be added
     */
    public void renewSession(Session oldSession, ServerHttpResponse response) {
        var filterContext = new HashMap<String, Object>();
        filterContext.put("old-session", oldSession);

        hooks.forEach(h -> h.renewSession(filterContext, response));
    }

    /**
     * Destroys an existing session by executing all session destruction hooks.
     * Each hook in the chain will have the opportunity to contribute to or modify
     * the session destruction process.
     *
     * @param exchange The server web exchange containing the HTTP request and response
     */
    public void destroySession(ServerWebExchange exchange){
        var filterContext = new HashMap<String, Object>();
        hooks.forEach(h -> h.destroySession(filterContext, exchange));
    }

    /**
     * Collects all session hooks from the application context and sorts them by order.
     *
     * @param context The Spring application context
     * @return A sorted list of session hooks
     */
    private List<SessionHook> getSessionHooks(ApplicationContext context) {
        var filters = context.getBeansOfType(SessionHook.class);

        List<SessionHook> sessionFilters = filters.values().stream()
                .sorted(Comparator.comparingInt(SessionHook::order))
                .collect(Collectors.toList());

        return sessionFilters;
    }
}
