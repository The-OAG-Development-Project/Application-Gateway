package ch.gianlucafrei.nellygateway.cookies;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.services.crypto.CookieDecryptionException;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieConverter {

    @Autowired
    NellyConfig config;

    @Autowired
    CookieEncryptor encryptor;

    public ResponseCookie convertStateCookie(LoginStateCookie stateCookie) {

        var value = encryptor.encryptObject(stateCookie);

        return ResponseCookie.from(LoginStateCookie.NAME, value)
                .httpOnly(true)
                .secure(config.isHttpsHost())
                .maxAge(Duration.ofMinutes(1))
                .path("/auth")
                .build();
    }

    public LoginStateCookie convertStateCookie(HttpCookie cookie) throws CookieDecryptionException {

        return encryptor.decryptObject(cookie.getValue(), LoginStateCookie.class);
    }

    public ResponseCookie convertCsrfCookie(CsrfCookie csrfCookie, int sessionDurationSeconds) {

        return ResponseCookie.from(CsrfCookie.NAME, csrfCookie.getCsrfToken())
                .httpOnly(false)
                .secure(config.isHttpsHost())
                .maxAge(Duration.ofSeconds(sessionDurationSeconds))
                .sameSite("Strict")
                .path("/")
                .build();
    }

    public CsrfCookie convertCsrfCookie(HttpCookie cookie) {
        return new CsrfCookie(cookie.getValue());
    }

    public ResponseCookie convertLoginCookie(LoginCookie loginCookie, int sessionDurationSeconds) {

        var value = encryptor.encryptObject(loginCookie);

        var sameSiteValue = config.isHttpsHost() ? "None" : null;

        return ResponseCookie.from(LoginCookie.NAME, value)
                .httpOnly(true)
                .secure(config.isHttpsHost())
                .maxAge(Duration.ofSeconds(sessionDurationSeconds))
                .sameSite(sameSiteValue)
                .path("/")
                .build();
    }

    public LoginCookie convertLoginCookie(HttpCookie cookie) throws CookieDecryptionException {

        return encryptor.decryptObject(cookie.getValue(), LoginCookie.class);
    }
}
