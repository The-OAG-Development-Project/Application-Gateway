package org.owasp.oag.controllers;

import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CookieConverter;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.exception.CookieDecryptionException;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.hooks.session.SessionHookChain;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.infrastructure.factories.LoginDriverFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.owasp.oag.services.csrf.CsrfSamesiteStrictValidation;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.owasp.oag.services.login.drivers.LoginDriverResult;
import org.owasp.oag.session.Session;
import org.owasp.oag.utils.LoggingUtils;
import org.owasp.oag.utils.ReactiveUtils;
import org.owasp.oag.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    ApplicationContext context;
    @Autowired
    private MainConfig config;
    @Autowired
    private LoginDriverFactory loginDriverFactory;
    @Autowired
    private CookieConverter cookieConverter;
    @Autowired
    private SessionHookChain sessionHookChain;
    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    @GetMapping("session")
    public SessionInformation sessionInfo(ServerWebExchange exchange) {
        SessionInformation sessionInformation;
        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);

        if (sessionOptional.isPresent()) {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_AUTHENTICATED);
            sessionInformation.setExpiresIn(sessionOptional.get().getRemainingTimeSeconds());

        } else {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return sessionInformation;
    }

    @GetMapping("{providerKey}/login")
    public Mono<ResponseEntity<Object>> login(
            @PathVariable(value = "providerKey") String providerKey,
            ServerWebExchange exchange) {

        var response = exchange.getResponse();

        // Load login implementation
        LoginDriver loginDriver = loadLoginDriver(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);

        // This might be blocking, encapsulate it in mono
        return ReactiveUtils.runBlockingProcedure(() -> loginDriver.startLogin(callbackURI))
                .map(loginDriverResult -> {

                    // Store login state
                    String returnUrl = loadLoginReturnUrl(exchange);
                    storeLoginState(providerKey, loginDriverResult, returnUrl, response);

                    // Redirect the user
                    var redirectUri = loginDriverResult.getAuthURI().toString();
                    return ResponseEntity.status(302).header("Location", redirectUri).build();
                });
    }

    public LoginDriver loadLoginDriver(String providerKey) {

        // Load settings
        LoginProvider provider = loadProvider(providerKey);
        String driverName = provider.getType();

        try {
            return loginDriverFactory.loadDriverByKey(driverName, provider.getWith());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "could not find login driver", e);
        }
    }

    public String loadLoginReturnUrl(ServerWebExchange exchange) {

        String returnUrl = exchange.getRequest().getQueryParams().getFirst("returnUrl");

        // Validate return url
        if (returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.getSessionBehaviour().getRedirectLoginSuccess();
        }

        if (!isValidReturnUrl(returnUrl)) {
            LoggingUtils.logInfo(log, exchange, "Received invalid returnUrl: " + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");
        }


        return returnUrl;
    }

    private void storeLoginState(
            String providerKey,
            LoginDriverResult loginDriverResult,
            String returnUrl,
            ServerHttpResponse response) {

        LoginStateCookie stateCookie = new LoginStateCookie(providerKey, loginDriverResult.getState(), returnUrl);
        response.addCookie(cookieConverter.convertStateCookie(stateCookie));
    }

    private LoginProvider loadProvider(String providerKey) {

        var provider = config.getLoginProviders().get(providerKey);

        if (provider == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found");

        return provider;
    }

    private URI loadCallbackURI(String providerKey) {

        try {
            String callbackStr = String.format("%s/auth/%s/callback", config.getHostUri(), providerKey);
            return new URI(callbackStr);

        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not compute login callback");
        }
    }

    public boolean isValidReturnUrl(String returnUrl) {

        ArrayList<String> allowedHosts = new ArrayList<>(config.getTrustedRedirectHosts());
        allowedHosts.add(config.getHostname());
        return UrlUtils.isValidReturnUrl(returnUrl, allowedHosts.toArray(new String[]{}), config.isHttpsHost());
    }

    @GetMapping("{providerKey}/callback")
    public Mono<ResponseEntity<Object>> callback(
            @PathVariable(value = "providerKey") String providerKey,
            ServerWebExchange exchange) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();

        // Load login implementation
        LoginDriver loginDriver = loadLoginDriver(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);

        // Load login state
        var loginState = loadLoginState(request);

        return ReactiveUtils.runBlockingProcedure(() -> loginDriver.processCallback(request, loginState.getState(), callbackURI))
                .onErrorMap(AuthenticationException.class, e -> {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                })
                .map(userModel -> {

                    // Store session
                    sessionHookChain.createSession(providerKey, userModel, response);

                    var redirectUri = loginState.getReturnUrl();
                    return ResponseEntity.status(302).header("Location", redirectUri).build();

                });
    }

    private LoginStateCookie loadLoginState(ServerHttpRequest request) {

        HttpCookie oidcCookie = request.getCookies().getFirst(LoginStateCookie.NAME);

        if (oidcCookie == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No login state");


        try {
            // Try decrypt
            return cookieConverter.convertStateCookie(oidcCookie);

        } catch (CookieDecryptionException e) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid cookie");
        }
    }

    @GetMapping("logout")
    public void logout(
            ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        // Logout csrf prevention
        CsrfProtectionValidation csrfValidation = getCsrfValidationMethod();
        if (csrfValidation.shouldBlockRequest(exchange, null)) {
            LoggingUtils.logInfo(log, exchange, "Blocked logout request due to csrf protection");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            // Destroy the user session
            sessionHookChain.destroySession(exchange);

            // Get default redirection url
            String returnUrl = loadLogoutReturnUrl(request);

            // To load the federated logout url we need the instance of the login provider
            Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);
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
            response.getHeaders().add("Location", returnUrl);
            response.setRawStatusCode(302);
        }
    }

    private CsrfProtectionValidation getCsrfValidationMethod() {

        return csrfValidationImplementationFactory.loadCsrfValidationImplementation(CsrfSamesiteStrictValidation.NAME);
    }

    public String loadLogoutReturnUrl(ServerHttpRequest request) {

        String returnUrl = request.getQueryParams().getFirst("returnUrl");

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
