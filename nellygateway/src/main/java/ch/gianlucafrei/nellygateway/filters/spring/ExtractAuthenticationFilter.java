package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.services.blacklist.SessionBlacklist;
import ch.gianlucafrei.nellygateway.services.crypto.CookieDecryptionException;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.session.Session;
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

import static ch.gianlucafrei.nellygateway.utils.LoggingUtils.*;

@Order(3)
@Component
public class ExtractAuthenticationFilter implements WebFilter {

    public final static String NELLY_SESSION = "nelly-session"; // Key for request context
    public final static String NELLY_SESSION_CSRF_TOKEN = "session-csrf-token";

    private static final Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    GlobalClockSource globalClockSource;

    @Autowired
    SessionBlacklist sessionBlacklist;

    public static Optional<Session> extractSessionFromExchange(ServerWebExchange exchange) {

        var sessionOptional = (Optional<Session>) exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional == null)
            return Optional.empty();

        return sessionOptional;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        logTrace(log, exchange, "Execute ExtractAuthenticationFilter");

        return extractAndStoreAuthentication(exchange)
                .then(chain.filter(exchange));
    }

    protected Mono<Void> extractAndStoreAuthentication(ServerWebExchange exchange) {

        exchange.getAttributes().put(NELLY_SESSION, Optional.empty());

        // Extract session from cookie
        var loginCookieOptional = extractLoginCookieFromRequest(exchange);

        if (loginCookieOptional.isEmpty())
            return Mono.empty();

        var loginCookie = loginCookieOptional.get();

        return sessionBlacklist.isInvalidated(loginCookie.getId())
                .doOnSuccess(isInvalidated -> {

                    if (isInvalidated) {
                        logInfo(log, exchange, "Received invalidated session cookie with id: {} user-id: {}", loginCookie.getId(), loginCookie.getUserModel().getId());
                    } else {

                        var sessionOptional = Session.fromSessionCookie(loginCookie, globalClockSource.getGlobalClock());

                        if(sessionOptional.isEmpty()){
                            logDebug(log, exchange, "Received invalid session cookie, maybe expired");
                        }

                        // Extract csrf token from session cookie and store in context
                        exchange.getAttributes().put(NELLY_SESSION_CSRF_TOKEN, loginCookie.getCsrfToken());
                        // Store session optional in http request object
                        exchange.getAttributes().put(NELLY_SESSION, sessionOptional);
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

            logInfo(log, exchange,"Received invalid session cookie");
        }

        return Optional.ofNullable(loginCookie);
    }
}