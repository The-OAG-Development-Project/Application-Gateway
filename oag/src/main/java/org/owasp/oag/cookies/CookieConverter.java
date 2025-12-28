package org.owasp.oag.cookies;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.services.crypto.CookieEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Component for converting between different cookie types.
 */
@Component
public class CookieConverter {

    private final MainConfig config;

    private final CookieEncryptor encryptor;

    /**
     * Constructor for CookieConverter.
     *
     * @param config    The main configuration.
     * @param encryptor The cookie encryptor.
     */
    @Autowired
    public CookieConverter(@Lazy MainConfig config, @Lazy CookieEncryptor encryptor) {
        this.config = config;
        this.encryptor = encryptor;
    }

    /**
     * Converts a LoginStateCookie to a ResponseCookie.
     *
     * @param stateCookie The LoginStateCookie to convert.
     * @return The converted ResponseCookie.
     */
    public ResponseCookie convertStateCookie(LoginStateCookie stateCookie) {

        var value = encryptor.encryptObject(stateCookie);

        return ResponseCookie.from(LoginStateCookie.NAME, value)
                .httpOnly(true)
                .secure(config.isHttpsHost())
                .maxAge(Duration.ofMinutes(1))
                .path("/auth")
                .build();
    }

    /**
     * Converts an HttpCookie to a LoginStateCookie.
     *
     * @param cookie The HttpCookie to convert.
     * @return The converted LoginStateCookie.
     * @throws CookieDecryptionException if the cookie cannot be decrypted.
     */
    public LoginStateCookie convertStateCookie(HttpCookie cookie) throws CookieDecryptionException {

        return encryptor.decryptObject(cookie.getValue(), LoginStateCookie.class);
    }

    /**
     * Converts a CsrfCookie to a ResponseCookie.
     *
     * @param csrfCookie            The CsrfCookie to convert.
     * @param sessionDurationSeconds The session duration in seconds.
     * @return The converted ResponseCookie.
     */
    public ResponseCookie convertCsrfCookie(CsrfCookie csrfCookie, int sessionDurationSeconds) {

        return ResponseCookie.from(CsrfCookie.NAME, csrfCookie.getCsrfToken())
                .httpOnly(false)
                .secure(config.isHttpsHost())
                .maxAge(Duration.ofSeconds(sessionDurationSeconds))
                .sameSite("Strict")
                .path("/")
                .build();
    }

    /**
     * Converts an HttpCookie to a CsrfCookie.
     *
     * @param cookie The HttpCookie to convert.
     * @return The converted CsrfCookie.
     */
    public CsrfCookie convertCsrfCookie(HttpCookie cookie) {
        return new CsrfCookie(cookie.getValue());
    }

    /**
     * Converts a LoginCookie to a ResponseCookie.
     *
     * @param loginCookie           The LoginCookie to convert.
     * @param sessionDurationSeconds The session duration in seconds.
     * @return The converted ResponseCookie.
     */
    public ResponseCookie convertLoginCookie(LoginCookie loginCookie, int sessionDurationSeconds) {

        var value = encryptor.encryptObject(loginCookie);

        var sameSiteValue = config.isHttpsHost() ? "None" : null;

        return ResponseCookie.from(LoginCookie.NAME, value)
                .httpOnly(true)
                .secure(config.isHttpsHost())
                .maxAge(sessionDurationSeconds)
                .sameSite(sameSiteValue)
                .path("/")
                .build();
    }

    /**
     * Converts an HttpCookie to a LoginCookie.
     *
     * @param cookie The HttpCookie to convert.
     * @return The converted LoginCookie.
     * @throws CookieDecryptionException if the cookie cannot be decrypted.
     */
    public LoginCookie convertLoginCookie(HttpCookie cookie) throws CookieDecryptionException {

        return encryptor.decryptObject(cookie.getValue(), LoginCookie.class);
    }
}
