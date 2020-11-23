package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProvider;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.cookies.LoginCookie;
import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieDecryptionException;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.services.login.drivers.AuthenticationException;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import ch.gianlucafrei.nellygateway.services.login.drivers.github.GitHubDriver;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriver;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriverResult;
import ch.gianlucafrei.nellygateway.services.login.drivers.oidc.OidcDriver;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private CookieEncryptor cookieEncryptor;

    @Autowired
    private NellyConfig config;

    @GetMapping("{providerKey}/login")
    public void login(
            @PathVariable(value = "providerKey") String providerKey,
            HttpServletResponse response,
            HttpServletRequest request){

            // Load login implementation
            LoginDriver loginDriver = loadLoginDriver(providerKey);
            LoginDriverResult loginDriverResult = loginDriver.startLogin();

            // Store login state
            String returnUrl = loadLoginReturnUrl(request);
            storeLoginState(providerKey, loginDriverResult, returnUrl, response);

            // Redirect the user
            response.setHeader("Location", loginDriverResult.getAuthURI().toString());
            response.setStatus(302);
    }

    @GetMapping("{providerKey}/callback")
    public void callback(
            @PathVariable(value = "providerKey") String providerKey,
            HttpServletResponse response,
            HttpServletRequest request){

            // Load login implementation
            LoginDriver loginDriver = loadLoginDriver(providerKey);

            // Load login state
            var loginState = loadLoginState(request);

            try {

                // Process Callback
                UserModel model = loginDriver.processCallback(request, loginState.getState());

                // Store session
                createSession(providerKey, model, response);

                // Redirect the user
                response.setHeader("Location", loginState.getReturnUrl());
                response.setStatus(302);

            } catch (AuthenticationException e) {

                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

    }

    @GetMapping("loggout")
    public void logout(
            HttpServletResponse response,
            HttpServletRequest request){

        // TODO add csrf protection

        destroySession(response);

        // Redirect the user
        String returnUrl = loadLogoutReturnUrl(request);

        // Redirect the user
        response.setHeader("Location", returnUrl);
        response.setStatus(302);
    }

    public String loadLoginReturnUrl(HttpServletRequest request) {

        String returnUrl = request.getParameter("returnUrl");

        // Validate return url
        if(returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.sessionBehaviour.getRedirectLoginSuccess();
        }

        if(! isValidReturnUrl(returnUrl))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");

        return returnUrl;
    }

    public String loadLogoutReturnUrl(HttpServletRequest request) {

        String returnUrl = request.getParameter("returnUrl");

        // Validate return url
        if(returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.sessionBehaviour.getRedirectLogout();
        }

        if(! isValidReturnUrl(returnUrl))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");

        return returnUrl;
    }



    public boolean isValidReturnUrl(String returnUrl){

        ArrayList<String> allowedHosts = new ArrayList<>(config.trustedRedirectHosts);
        allowedHosts.add(config.getHostname());
        return  UrlUtils.isValidReturnUrl(returnUrl, allowedHosts.toArray(new String[]{}));
    }

    private LoginProvider loadProvider(String providerKey) {

        var provider = config.loginProviders.get(providerKey);

        if(provider == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found");

        return provider;
    }

    private URI loadCallbackURI(String providerKey){

        try {
            String callbackStr = String.format("%s/auth/%s/callback", config.hostUri, providerKey);
            return new URI(callbackStr);

        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not compute login callback");
        }
    }

    private LoginDriver loadLoginDriver(String providerKey){

        // Load settings
        LoginProvider provider = loadProvider(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);

        // Load login driver
        String driverName = provider.getType();

        if("oidc".equals(driverName))
            return new OidcDriver(provider.getWith(), callbackURI);

        if("github".equals(driverName))
            return new GitHubDriver(provider.getWith(), callbackURI);

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not find login driver");
    }

    private void storeLoginState(
            String providerKey,
            LoginDriverResult loginDriverResult,
            String returnUrl,
            HttpServletResponse response) {

        LoginStateCookie stateCookie = new LoginStateCookie(providerKey, loginDriverResult.getState(), returnUrl);
        String encryptedStateCookie = cookieEncryptor.encryptObject(stateCookie);
        Cookie cookie = new Cookie(LoginStateCookie.NAME, encryptedStateCookie);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    private LoginStateCookie loadLoginState(HttpServletRequest request) {

        Cookie oidcCookie = CookieUtils.getCookieOrNull(LoginStateCookie.NAME, request);

        if(oidcCookie == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No login state");


        try {
            // Try decrypt
            return cookieEncryptor.decryptObject(oidcCookie.getValue(), LoginStateCookie.class);

        } catch (CookieDecryptionException e) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid cookie");
        }
    }

    private void createSession(String providerKey, UserModel model, HttpServletResponse response) {

        int currentTimeSeconds = (int)(System.currentTimeMillis() / 1000);
        int sessionDuration = config.sessionBehaviour.getSessionDuration();
        int sessionExp = currentTimeSeconds + sessionDuration;

        LoginCookie loginCookie = new LoginCookie(sessionExp, providerKey, model);
        String encryptedLoginCookie = cookieEncryptor.encryptObject(loginCookie);

        Cookie cookie = new Cookie(LoginCookie.NAME, encryptedLoginCookie);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(sessionDuration);
        cookie.setSecure(config.isHttpsHost());
        CookieUtils.addSameSiteCookie(cookie, LoginCookie.SAMESITE, response);
    }

    private void destroySession(HttpServletResponse response){

        // Override session cookie with new cookie that has max-age = 0
        Cookie cookie = new Cookie(LoginCookie.NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(config.isHttpsHost());
        CookieUtils.addSameSiteCookie(cookie, LoginCookie.SAMESITE, response);
    }
}
