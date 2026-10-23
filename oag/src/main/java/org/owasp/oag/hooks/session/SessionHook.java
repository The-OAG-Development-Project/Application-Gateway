package org.owasp.oag.hooks.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Interface for session-related hook operations.
 * Session hooks are called during session lifecycle events such as creation, renewal and destruction.
 */
public interface SessionHook {

    /**
     * Renews an existing session, typically by creating a new session that replaces the old one.
     *
     * @param filterContext The context containing session information and other data
     * @param response The response where cookies or headers can be added
     */
    void renewSession(Map<String, Object> filterContext, HttpServletResponse response);

    /**
     * Defines the execution order of this hook in the chain of hooks. Lower values run first.
     *
     * @return The order value for this hook
     */
    int order();

    /**
     * Creates a new session.
     *
     * @param filterContext The context containing the data needed for session creation
     * @param response The response where cookies or headers can be added
     */
    void createSession(Map<String, Object> filterContext, HttpServletResponse response);

    /**
     * Destroys an existing session, typically by invalidating cookies or removing session state.
     *
     * @param filterContext The context containing session information and other data
     * @param request The current request
     * @param response The response where cookies or headers can be added
     */
    void destroySession(Map<String, Object> filterContext, HttpServletRequest request, HttpServletResponse response);
}
