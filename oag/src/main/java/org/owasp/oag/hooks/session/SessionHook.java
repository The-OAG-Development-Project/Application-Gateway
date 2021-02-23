package org.owasp.oag.hooks.session;

import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface SessionHook {

    static void runRenewSessionFilterChain(ApplicationContext context, Map<String, Object> filterContext, ServerHttpResponse response) {

        var filters = getSessionHooks(context);
        filters.forEach(f -> f.renewSession(filterContext, response));
    }

    static List<SessionHook> getSessionHooks(ApplicationContext context) {

        var filters = context.getBeansOfType(SessionHook.class);

        List<SessionHook> sessionFilters = filters.values().stream()
                .sorted(Comparator.comparingInt(SessionHook::order))
                .collect(Collectors.toList());

        return sessionFilters;
    }

    void renewSession(Map<String, Object> filterContext, ServerHttpResponse response);

    int order();

    void createSession(Map<String, Object> filterContext, ServerHttpResponse response);

    void destroySession(Map<String, Object> filterContext, ServerWebExchange response);
}
