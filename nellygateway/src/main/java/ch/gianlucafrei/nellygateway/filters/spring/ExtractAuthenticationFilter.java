package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieDecryptionException;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(3)
@Component
public class ExtractAuthenticationFilter extends GlobalFilterBase {

    public final static String NELLY_SESSION = "nelly-session"; // Key for request context
    public final static String NELLY_SESSION_CSRF_TOKEN = "session-csrf-token";

    private static final Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Autowired
    GlobalClockSource globalClockSource;

    @Override
    protected void filter() {

        // Extract session from cookie
        HttpCookie cookie = request.getCookies().getFirst(LoginCookie.NAME);
        Optional<Session> sessionOptional;
        if (cookie == null) {
            sessionOptional = Optional.empty();
        } else {
            try {
                // Decrypt cookie
                LoginCookie loginCookie = cookieEncryptor.decryptObject(cookie.getValue(), LoginCookie.class);
                sessionOptional = Session.fromSessionCookie(loginCookie, globalClockSource.getGlobalClock());

                // Extract csrf token from session cookie and store in context
                exchange.getAttributes().put(NELLY_SESSION_CSRF_TOKEN, loginCookie.getCsrfToken());

            } catch (CookieDecryptionException e) {

                log.info("Received invalid session cookie");
                sessionOptional = Optional.empty();
            }
        }

        // Store session optional in http request object
        exchange.getAttributes().put(NELLY_SESSION, sessionOptional);
    }
}