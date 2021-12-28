package org.owasp.oag.filters.spring;

import org.jetbrains.annotations.NotNull;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.services.blacklist.SessionBlacklist;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Order(40)
@Component
public class ExtractAuthenticationFilter implements WebFilter {

    public final static String OAG_SESSION = "oag-session"; // Key for request context
    public final static String OAG_SESSION_CSRF_TOKEN = "oag-session-csrf-token";

    private static final Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    GlobalClockSource globalClockSource;

    @Autowired
    SessionBlacklist sessionBlacklist;

    public static Optional<Session> extractSessionFromExchange(ServerWebExchange exchange) {

        Object sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional == null)
            return Optional.empty();

        if (sessionOptional instanceof Optional) {
            //noinspection unchecked
            return (Optional<Session>) sessionOptional;
        } else {
            LoggingUtils.logWarn(log, exchange, "oag-session attribute is of incompatible type!! Fix it.");
            return  Optional.empty();
        }
    }

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {

        LoggingUtils.logTrace(log, exchange, "Execute ExtractAuthenticationFilter");

        return extractAndStoreAuthentication(exchange)
                .then(chain.filter(exchange));
    }

    protected Mono<Void> extractAndStoreAuthentication(ServerWebExchange exchange) {

        exchange.getAttributes().put(OAG_SESSION, Optional.empty());

        // Extract session from cookie
        var loginCookieOptional = extractLoginCookieFromRequest(exchange);

        if (loginCookieOptional.isEmpty())
            return Mono.empty();

        var loginCookie = loginCookieOptional.get();

        return sessionBlacklist.isInvalidated(loginCookie.getId())
                .doOnSuccess(isInvalidated -> {

                    if (isInvalidated) {
                        LoggingUtils.logInfo(log, exchange, "Received invalidated session cookie with id: {} user-id: {}", loginCookie.getId(), loginCookie.getUserModel().getId());
                    } else {

                        var sessionOptional = Session.fromSessionCookie(loginCookie, globalClockSource.getGlobalClock());

                        if(sessionOptional.isEmpty()){
                            LoggingUtils.logDebug(log, exchange, "Received invalid session cookie, maybe expired");
                        }

                        // Extract csrf token from session cookie and store in context
                        exchange.getAttributes().put(OAG_SESSION_CSRF_TOKEN, loginCookie.getCsrfToken());
                        // Store session optional in http request object
                        exchange.getAttributes().put(OAG_SESSION, sessionOptional);
                    }

                }).then();
    }

    protected Optional<LoginCookie> extractLoginCookieFromRequest(ServerWebExchange exchange) {

        LoginCookie loginCookie = null;
        var request = exchange.getRequest();
        HttpCookie cookie = request.getCookies().getFirst(LoginCookie.NAME);


        if (cookie == null)
            return Optional.empty();

        try {
            // Decrypt cookie
            loginCookie = cookieEncryptor.decryptObject(cookie.getValue(), LoginCookie.class);

        } catch (CookieDecryptionException e) {

            LoggingUtils.logInfo(log, exchange,"Received invalid session cookie");
        }

        return Optional.ofNullable(loginCookie);
    }
}