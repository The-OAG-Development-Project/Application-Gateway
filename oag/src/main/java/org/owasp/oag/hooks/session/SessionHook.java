package org.owasp.oag.hooks.session;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * Interface for session-related hook operations.
 * Session hooks are called during session lifecycle events such as creation, renewal, and destruction.
 * Each hook can perform a specific action during these events, such as creating cookies or
 * updating session state.
 */
public interface SessionHook {

    /**
     * Renews an existing session, typically by creating a new session that replaces the old one.
     *
     * @param filterContext The context containing session information and other data
     * @param response The HTTP response where cookies or headers can be added
     */
    void renewSession(Map<String, Object> filterContext, ServerHttpResponse response);

    /**
     * Defines the execution order of this hook in the chain of hooks.
     * Lower values indicate higher priority.
     *
     * @return The order value for this hook
     */
    int order();

    /**
     * Creates a new session.
     *
     * @param filterContext The context containing user information and other data needed for session creation
     * @param response The HTTP response where cookies or headers can be added
     */
    void createSession(Map<String, Object> filterContext, ServerHttpResponse response);

    /**
     * Destroys an existing session, typically by invalidating cookies or removing session state.
     *
     * @param filterContext The context containing session information and other data
     * @param response The server web exchange containing the HTTP request and response
     */
    void destroySession(Map<String, Object> filterContext, ServerWebExchange response);
}
