package org.owasp.oag.hooks.session;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public interface SessionHook {

    void renewSession(Map<String, Object> filterContext, ServerHttpResponse response);

    int order();

    void createSession(Map<String, Object> filterContext, ServerHttpResponse response);

    void destroySession(Map<String, Object> filterContext, ServerWebExchange response);
}
