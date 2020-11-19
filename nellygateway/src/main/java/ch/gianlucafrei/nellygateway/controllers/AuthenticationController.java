package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.AuthProvider;
import ch.gianlucafrei.nellygateway.cookies.OidcStateCookie;
import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCCallbackResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCLoginStepResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCService;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.DiverConfiguration;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import ch.gianlucafrei.nellygateway.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private DiverConfiguration driverConfiguration;

    @Autowired
    private CookieEncryptor cookieEncryptor;

    @GetMapping("nelly")
    public String index() {
        return "This is Nelly";
    }

    @GetMapping("{providerKey}/login")
    public void login(
            @PathVariable(value = "providerKey") String providerKey,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletResponse httpServletResponse) {
        log.trace(String.format("auth login request"));

        // Load auth provider settings
        AuthProvider providerSettings = NellygatewayApplication.config.authProviders.getOrDefault(providerKey, null);
        if (providerSettings == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Provider not found"
            );
        }

        // Load driver
        OIDCService oidcService = driverConfiguration.getDriver(providerSettings.getDriver());

        // Validate return url
        if(returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            returnUrl = providerSettings.getRedirectSuccess();
        }
        else{
            if(! isValidReturnUrl(returnUrl))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");
        }

        // Create OIDC request
        String callbackUri = String.format("%s/auth/%s/callback", NellygatewayApplication.config.hostUri, providerKey);
        OIDCLoginStepResult result = oidcService.getRedirectUri(providerSettings, callbackUri);

        // Store state and nonce as a encrypted JEW in cookie on the client
        OidcStateCookie oidcState = new OidcStateCookie(providerKey, result.state, result.nonce, returnUrl);
        String encryptedState = cookieEncryptor.encryptObject(oidcState);
        Cookie oidcStateCookie = new Cookie("oidc-state", encryptedState);
        httpServletResponse.addCookie(oidcStateCookie);

        // Redirect the user
        httpServletResponse.setHeader("Location", result.redirectUri);
        httpServletResponse.setStatus(302);
    }

    @GetMapping("{providerKey}/callback")
    public void loginCallback(
            @PathVariable(value = "providerKey") String providerKey,
            @RequestParam("code") String codeStr,
            @RequestParam("state") String stateStr,
            HttpServletResponse response,
            HttpServletRequest request) {
        log.trace(String.format("auth callback request"));

        // Load oidc state from cookie
        Cookie oidcCookie = CookieUtils.getCookieOrNull("oidc-state", request);
        OidcStateCookie oidcState = cookieEncryptor.decryptObject(oidcCookie.getValue(), OidcStateCookie.class);

        if (!providerKey.equals(oidcState.getProvider())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provider error"
            );
        }

        AuthProvider providerSettings = NellygatewayApplication.config.authProviders.getOrDefault(providerKey, null);

        // Load driver
        OIDCService oidcService = driverConfiguration.getDriver(providerSettings.getDriver());

        String callbackUri = String.format("%s/auth/%s/callback", NellygatewayApplication.config.hostUri, providerKey);

        OIDCCallbackResult result = oidcService.processCallback(providerSettings, codeStr, callbackUri);

        if (!result.success) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad request"
            );
        }

        // Create Session Cookie
        long sessionCreationTime = System.currentTimeMillis() / 1000;
        long sessionExpireTime = sessionCreationTime + providerSettings.getSessionDuration();

        SessionCookie sessionCookie = new SessionCookie();
        sessionCookie.setOrginalToken(result.originalToken);
        sessionCookie.setProvider(providerKey);
        sessionCookie.setSubject(result.subject);
        sessionCookie.setSessionExp(sessionExpireTime);

        Cookie cookie = sessionCookie.getEncryptedHttpCookie(cookieEncryptor, providerSettings.getSessionDuration());
        CookieUtils.addSameSiteCookie(cookie, SessionCookie.SAMESITE, response);

        // Redirect the user
        response.setHeader("Location", oidcState.getReturnUrl());
        response.setStatus(302);
    }

    @GetMapping("/logout")
    public void logout(
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletResponse response,
            HttpServletRequest request) {
        log.trace(String.format("logout request"));

        //TODO Logout CSRF Protection

        // Override session cookie with new cookie that has max-age = 0
        SessionCookie sessionCookie = new SessionCookie();
        Cookie cookie = sessionCookie.getEncryptedHttpCookie(cookieEncryptor, 0);
        CookieUtils.removeSameSiteCookie(cookie, SessionCookie.SAMESITE, response);

        // Redirect the user

        String redirectUrl = NellygatewayApplication.config.logoutRedirectUri;
        if(returnUrl != null)
        {
            // validate return url
            // if not valid we fail silent and redirect to the default url
            if(isValidReturnUrl(returnUrl))
                redirectUrl = returnUrl;
            else
                log.warn("Received invalid return url during logout. Redirect to default logout url");
        }

        response.setHeader("Location", redirectUrl);
        response.setStatus(302);
    }

    public boolean isValidReturnUrl(String returnUrl){

        ArrayList<String> allowedHosts = new ArrayList<>(NellygatewayApplication.config.trustedRedirectHosts);
        allowedHosts.add(NellygatewayApplication.config.getHostname());
        return  UrlUtils.isValidReturnUrl(returnUrl, allowedHosts.toArray(new String[]{}));
    }
}
