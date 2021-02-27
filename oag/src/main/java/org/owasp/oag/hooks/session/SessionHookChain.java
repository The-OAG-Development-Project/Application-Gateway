package org.owasp.oag.hooks.session;

import org.owasp.oag.services.login.drivers.UserModel;
import org.owasp.oag.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionHookChain {

    private List<SessionHook> hooks;

    public SessionHookChain(@Autowired ApplicationContext context){

        hooks = getSessionHooks(context);
    }

    public void createSession(String providerKey, UserModel model, ServerHttpResponse response) {

        var filterContext = new HashMap<String, Object>();
        filterContext.put("providerKey", providerKey);
        filterContext.put("userModel", model);

        hooks.forEach(h -> h.createSession(filterContext, response));
    }

    public void renewSession(Session oldSession, ServerHttpResponse response) {

        var filterContext = new HashMap<String, Object>();
        filterContext.put("old-session", oldSession);

        hooks.forEach(h -> h.renewSession(filterContext, response));
    }

    public void destroySession(ServerWebExchange exchange){

        var filterContext = new HashMap<String, Object>();
        hooks.forEach(h -> h.destroySession(filterContext, exchange));
    }

    private List<SessionHook> getSessionHooks(ApplicationContext context) {

        var filters = context.getBeansOfType(SessionHook.class);

        List<SessionHook> sessionFilters = filters.values().stream()
                .sorted(Comparator.comparingInt(SessionHook::order))
                .collect(Collectors.toList());

        return sessionFilters;
    }
}
