package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.AuthProvider;
import ch.gianlucafrei.nellygateway.cookies.OidcStateCookie;
import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCCallbackResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCLoginStepResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCService;
import ch.gianlucafrei.nellygateway.utils.CookieUtils;
import ch.gianlucafrei.nellygateway.utils.JWEGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    private JWEGenerator jweGenerator = new JWEGenerator();
    private OIDCService oidcService = new OIDCService();

    @GetMapping("nelly")
    public String index() {
        return "This is Nelly";
    }

    @GetMapping("{providerKey}/login")
    public void login(@PathVariable(value = "providerKey") String providerKey, HttpServletResponse httpServletResponse) throws URISyntaxException {
        log.info(String.format("auth login request"));

        // Load auth provider settings
        AuthProvider providerSettings = NellygatewayApplication.config.authProviders.getOrDefault(providerKey, null);
        if (providerSettings == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Provider not found"
            );
        }

        // Create OIDC request
        String callbackUri = String.format("%s/auth/%s/callback", NellygatewayApplication.config.hostUri, providerKey);
        OIDCLoginStepResult result = oidcService.getRedirectUri(providerSettings, callbackUri);

        // Store state and nonce as a encrypted JEW in cookie on the client
        OidcStateCookie oidcState = new OidcStateCookie(providerKey, result.state, result.nonce);
        String encryptedState = jweGenerator.encryptObject(oidcState);
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
        log.info(String.format("auth callback request"));


        // Load oidc state from cookie
        Cookie oidcCookie = CookieUtils.getCookieOrNull("oidc-state", request);
        OidcStateCookie oidcState = jweGenerator.decryptObject(oidcCookie.getValue(), OidcStateCookie.class);

        if (!providerKey.equals(oidcState.getProvider())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provider error"
            );
        }

        AuthProvider providerSettings = NellygatewayApplication.config.authProviders.getOrDefault(providerKey, null);
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

        Cookie cookie = sessionCookie.getEncryptedHttpCookie(jweGenerator, providerSettings.getSessionDuration());
        response.addCookie(cookie);

        // Redirect the user
        response.setHeader("Location", providerSettings.getRedirectSuccess());
        response.setStatus(302);
    }

    @GetMapping("/logout")
    public void logout(
            HttpServletResponse response,
            HttpServletRequest request) {

        // Override session cookie with new cookie that has max-age = 0
        SessionCookie sessionCookie = new SessionCookie();
        Cookie cookie = sessionCookie.getEncryptedHttpCookie(jweGenerator, 0);
        response.addCookie(cookie);

        // Redirect the user
        response.setHeader("Location", NellygatewayApplication.config.logoutRedirectUri);
        response.setStatus(302);
    }
}
