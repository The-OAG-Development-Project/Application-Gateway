package org.owasp.oag.services.csrf;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.SecureEncoder.encodeStringForLog;

/**
 * Implementation of CSRF protection validation using SameSite=Strict cookies.
 * This class validates requests by comparing the CSRF token in the cookie with the one in the session.
 * It also performs additional validation by checking the Origin and Referer headers against the target origin.
 * This provides defense-in-depth protection against CSRF attacks, especially for browsers that don't
 * support SameSite=Strict cookies.
 */
@Component
public class CsrfSameSiteStrictCookieValidation implements CsrfProtectionValidation {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(CsrfSameSiteStrictCookieValidation.class);

    /**
     * Converter for handling cookies, including CSRF cookies.
     * Used to extract and convert CSRF tokens from cookies.
     */
    @Autowired
    CookieConverter cookieConverter;

    /**
     * Main configuration of the application.
     * Used to access the host URI for origin validation.
     */
    @Autowired
    MainConfig mainConfig;

    @Override
    public boolean needsRequestBody() {
        return false;
    }

    @Override
    public boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody) {

        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional != null && sessionOptional.isPresent()) {
            String csrfValueFromSession = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION_CSRF_TOKEN);
            String csrfValueFromCookie = extractCsrfToken(exchange.getRequest());

            if (csrfValueFromCookie == null) {
                logInfo(log, exchange, "Csrf cookie is missing");
                return true;
            }

            // As defense in-depth measure we also check the origin and referer header
            var originHeader = exchange.getRequest().getHeaders().getOrigin();
            var refererHeader = exchange.getRequest().getHeaders().getFirst("Referer");
            var targetOrigin = mainConfig.getHostUri();
            if (shouldBlockBasedOnOriginHeader(originHeader, refererHeader, targetOrigin)) {

                logInfo(log, exchange, "Origin/Referer header does not match target origin: originHeader='{}' refererHeader='{}' targetOrigin='{}'",
                        encodeStringForLog(originHeader, 100), encodeStringForLog(refererHeader, 100), targetOrigin);
                return true;
            }


            return !csrfValueFromCookie.equals(csrfValueFromSession);
        }

        return false;
    }

    /**
     * Extracts the CSRF token from the request cookies.
     *
     * @param request The server HTTP request containing the cookies
     * @return The CSRF token if found, or null if not present or invalid
     */
    private String extractCsrfToken(ServerHttpRequest request) {

        HttpCookie cookie = request.getCookies().getFirst(CsrfCookie.NAME);

        if (cookie == null)
            return null;

        CsrfCookie csrfCookie = cookieConverter.convertCsrfCookie(cookie);

        if (csrfCookie == null)
            return null;

        return csrfCookie.getCsrfToken();
    }

    /**
     * Returns true if the request should be blocked because the origin header is different from the target origin.
     * Because the samesite-strict cookies are not supported by all browser we use this defense is depth measure.
     *
     * @param originHeader          Origin header of the request or null if not present
     * @param refererHeader         Referer header of the request or null if not present
     * @param targetOriginUrlString Target origin (HostUri from settings)
     * @return <b>true</b>: Origin header different from target header <br/>
     *         <b>false</b>: otherwise
     */
    public boolean shouldBlockBasedOnOriginHeader(String originHeader, String refererHeader, String targetOriginUrlString) {

        if (targetOriginUrlString == null)
            return false;

        if (targetOriginUrlString.equals(originHeader))
            return false;

        URL targetOriginUrl, originalOriginUrl;
        try {
            targetOriginUrl = new URL(targetOriginUrlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("targetOriginUrlString is not a valid url");
        }

        String origin = originHeader;

        // fallback to referer if there is no origin header
        if (origin == null || "null".equals(origin)) {
            origin = refererHeader;
        }

        if (origin == null || "null".equals(origin)) {
            // If we cannot determine the origin at all teh request is not blocked
            return false;
        }

        // Both, origin and referer header should be valid urls, if they are not we don't block the request
        try {
            originalOriginUrl = new URL(origin);
        } catch (MalformedURLException e) {
            return false;
        }

        if (!targetOriginUrl.getProtocol().equals(originalOriginUrl.getProtocol())) {
            return true;
        }

        return !targetOriginUrl.getHost().equals(originalOriginUrl.getHost());
    }
}
