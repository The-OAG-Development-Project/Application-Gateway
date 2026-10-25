package org.owasp.oag.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.CookieWriter;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.hooks.session.SessionHookChain;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.owasp.oag.services.csrf.CsrfSameSiteStrictCookieValidation;
import org.owasp.oag.services.csrf.CsrfValidationRegistry;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.owasp.oag.services.login.drivers.LoginDriverRegistry;
import org.owasp.oag.services.login.drivers.LoginDriverResult;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

/**
 * REST controller for handling authentication endpoints, including login, callback,
 * session information, and logout. Manages user sessions and delegates authentication
 * to various login drivers.
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    /**
     * Application context for accessing beans.
     */
    @Autowired
    ApplicationContext context;

    /**
     * Main configuration of the application.
     */
    @Autowired
    private MainConfig config;

    /**
     * Registry for creating LoginDriver instances.
     */
    @Autowired
    private LoginDriverRegistry loginDriverRegistry;

    /**
     * Converter for handling cookies.
     */
    @Autowired
    private CookieConverter cookieConverter;

    /**
     * Chain of session hooks to be executed during session creation and destruction.
     */
    @Autowired
    private SessionHookChain sessionHookChain;

    /**
     * Factory for creating CSRF validation implementations.
     */
    @Autowired
    private CsrfValidationRegistry csrfValidationRegistry;

    /**
     * Initiates the login process for a given provider.
     *
     * @param providerKey The key of the login provider.
     * @param request     The current request.
     * @param response    The current response.
     * @return A ResponseEntity redirecting the user to the identity provider.
     */
    @GetMapping("{providerKey}/login")
    public ResponseEntity<Object> login(
            @PathVariable(value = "providerKey") String providerKey,
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginDriver loginDriver = loadLoginDriver(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);

        var loginDriverResult = loginDriver.startLogin(callbackURI);

        // Store login state
        String returnUrl = loadLoginReturnUrl(request);
        storeLoginState(providerKey, loginDriverResult, returnUrl, response);

        // Redirect the user
        var redirectUri = loginDriverResult.getAuthURI().toString();
        return ResponseEntity.status(302).header("Location", redirectUri).build();
    }

    /**
     * Loads the appropriate LoginDriver based on the provider key.
     *
     * @param providerKey The key of the login provider.
     * @return The loaded LoginDriver instance.
     * @throws ResponseStatusException if the login driver cannot be found.
     */
    public LoginDriver loadLoginDriver(String providerKey) {

        // Load settings
        LoginProvider provider = loadProvider(providerKey);
        String driverName = provider.getType();

        try {
            return loginDriverRegistry.load(driverName, provider.getWith());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "could not find login driver", e);
        }
    }

    /**
     * Retrieves session information for the current user.
     *
     * @param request The current request.
     * @return A SessionInformation object containing the session state.
     */
    @GetMapping("session")
    public SessionInformation sessionInfo(HttpServletRequest request) {

        SessionInformation sessionInformation;
        Optional<Session> sessionOptional = Session.REQUEST_SESSION.get();

        if (sessionOptional.isPresent()) {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_AUTHENTICATED);
            sessionInformation.setExpiresIn(sessionOptional.get().getRemainingTimeSeconds());

        } else {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return sessionInformation;
    }

    /**
     * Extracts the return URL from the request parameters.
     *
     * @param request The current request.
     * @return The extracted return URL.
     * @throws ResponseStatusException if the return URL is invalid.
     */
    public String loadLoginReturnUrl(HttpServletRequest request) {

        String returnUrl = request.getParameter("returnUrl");

        // Validate return url
        if (returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.getSessionBehaviour().getRedirectLoginSuccess();
        }

        if (!isValidReturnUrl(returnUrl)) {
            log.info("Received invalid returnUrl: {}.", URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");
        }


        return returnUrl;
    }

    /**
     * Stores the login state in a cookie.
     *
     * @param providerKey       The key of the login provider.
     * @param loginDriverResult The result of the login driver's startLogin method.
     * @param returnUrl         The URL to redirect to after successful login.
     * @param response          The current response.
     */
    private void storeLoginState(
            String providerKey,
            LoginDriverResult loginDriverResult,
            String returnUrl,
            HttpServletResponse response) {

        LoginStateCookie stateCookie = new LoginStateCookie(providerKey, loginDriverResult.getState(), returnUrl);
        CookieWriter.addCookie(response, cookieConverter.convertStateCookie(stateCookie));
    }

    /**
     * Loads the LoginProvider configuration based on the provider key.
     *
     * @param providerKey The key of the login provider.
     * @return The loaded LoginProvider configuration.
     * @throws ResponseStatusException if the provider is not found.
     */
    private LoginProvider loadProvider(String providerKey) {

        var provider = config.getLoginProviders().get(providerKey);

        if (provider == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found");

        return provider;
    }

    /**
     * Constructs the callback URI for a given provider.
     *
     * @param providerKey The key of the login provider.
     * @return The constructed callback URI.
     * @throws ResponseStatusException if the URI cannot be constructed.
     */
    private URI loadCallbackURI(String providerKey) {

        try {
            String callbackStr = String.format("%s/auth/%s/callback", config.getHostUri(), providerKey);
            return new URI(callbackStr);

        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not compute login callback");
        }
    }

    /**
     * Checks if a return URL is valid against a list of allowed hosts.
     *
     * @param returnUrl The return URL to validate.
     * @return True if the return URL is valid, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidReturnUrl(String returnUrl) {
        ArrayList<String> allowedHosts = new ArrayList<>(config.getTrustedRedirectHosts());
        allowedHosts.add(config.getHostname());
        return UrlUtils.isValidReturnUrl(returnUrl, allowedHosts.toArray(new String[]{}));
    }

    /**
     * Handles the callback from the login provider.
     *
     * @param providerKey The key of the login provider.
     * @param request     The current request.
     * @param response    The current response.
     * @return A ResponseEntity redirecting the user to the stored return url.
     * @throws ResponseStatusException if authentication fails or the login state is invalid.
     */
    @GetMapping("{providerKey}/callback")
    public ResponseEntity<Object> callback(
            @PathVariable(value = "providerKey") String providerKey,
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginDriver loginDriver = loadLoginDriver(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);
        var loginState = loadLoginState(request);

        try {
            var userModel = loginDriver.processCallback(request, loginState.getState(), callbackURI);

            // Store session
            sessionHookChain.createSession(providerKey, userModel, response);

            return ResponseEntity.status(302).header("Location", loginState.getReturnUrl()).build();

        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Loads the login state from the request cookies.
     *
     * @param request The current request.
     * @return The loaded LoginStateCookie.
     * @throws ResponseStatusException if the login state is missing or invalid.
     */
    private LoginStateCookie loadLoginState(HttpServletRequest request) {

        Optional<Cookie> oidcCookie = ExtractAuthenticationFilter.findCookie(request, LoginStateCookie.NAME);

        if (oidcCookie.isEmpty())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No login state");


        try {
            // Try decrypt
            return cookieConverter.convertStateCookie(oidcCookie.get());

        } catch (CookieDecryptionException e) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid cookie");
        }
    }

    /**
     * Handles the logout process, destroying the user session and redirecting to the logout URL.
     *
     * @param request  The current request.
     * @param response The current response.
     * @throws ResponseStatusException if CSRF validation fails.
     */
    @GetMapping("logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        // Logout csrf prevention
        CsrfProtectionValidation csrfValidation = getCsrfValidationMethod();
        if (csrfValidation.shouldBlockRequest(request, null)) {
            log.info("Blocked logout request due to csrf protection.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // Destroy the user session
        sessionHookChain.destroySession(request, response);

        // Get default redirection url
        String returnUrl = loadLogoutReturnUrl(request);

        // To load the federated logout url we need the instance of the login provider
        Optional<Session> sessionOptional = Session.REQUEST_SESSION.get();
        if (sessionOptional.isPresent()) {
            var session = sessionOptional.get();
            var provider = session.getProvider();
            var userModel = session.getUserModel();

            LoginDriver loginDriver = loadLoginDriver(provider);
            var federatedLogoutUrl = loginDriver.processFederatedLogout(userModel);

            if (federatedLogoutUrl != null)
                returnUrl = federatedLogoutUrl.toString();
        }

        // Redirect the user
        response.setStatus(302);
        response.setHeader("Location", returnUrl);
    }

    /**
     * Gets the CSRF validation implementation to use for logout protection.
     * This method returns a sameSiteStrictCookie protection implementation.
     *
     * @return The CSRF protection validation implementation
     */
    private CsrfProtectionValidation getCsrfValidationMethod() {
        // using the sameSiteStrictCookie protection for OWASP's own CSRF protection during logout
        return csrfValidationRegistry.get(CsrfSameSiteStrictCookieValidation.TYPE);
    }

    /**
     * Loads the logout return URL from the request parameters.
     *
     * @param request The current request.
     * @return The loaded logout return URL.
     * @throws ResponseStatusException if the return URL is invalid.
     */
    public String loadLogoutReturnUrl(HttpServletRequest request) {

        String returnUrl = request.getParameter("returnUrl");

        // Validate return url
        if (returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.getSessionBehaviour().getRedirectLogout();
        }

        if (!isValidReturnUrl(returnUrl))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");

        return returnUrl;
    }
}
