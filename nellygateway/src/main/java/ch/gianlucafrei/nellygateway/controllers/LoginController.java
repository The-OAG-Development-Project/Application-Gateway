package ch.gianlucafrei.nellygateway.controllers;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProvider;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.controllers.dto.SessionInformation;
import ch.gianlucafrei.nellygateway.cookies.CookieConverter;
import ch.gianlucafrei.nellygateway.cookies.LoginStateCookie;
import ch.gianlucafrei.nellygateway.filters.session.NellySessionFilter;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.services.crypto.CookieDecryptionException;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfProtectionValidation;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfSamesiteStrictValidation;
import ch.gianlucafrei.nellygateway.services.login.drivers.AuthenticationException;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriver;
import ch.gianlucafrei.nellygateway.services.login.drivers.LoginDriverResult;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import ch.gianlucafrei.nellygateway.services.login.drivers.oidc.LoginDriverLoader;
import ch.gianlucafrei.nellygateway.session.Session;
import ch.gianlucafrei.nellygateway.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    ApplicationContext context;
    @Autowired
    private NellyConfig config;
    @Autowired
    private LoginDriverLoader loginDriverLoader;
    @Autowired
    private CookieConverter cookieConverter;

    @GetMapping("session")
    public SessionInformation sessionInfo(ServerWebExchange exchange) {
        SessionInformation sessionInformation;
        Optional<Session> sessionOptional = (Optional<Session>) exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if (sessionOptional.isPresent()) {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_AUTHENTICATED);
            sessionInformation.setExpiresIn((int) sessionOptional.get().getRemainingTimeSeconds());

        } else {
            sessionInformation = new SessionInformation(SessionInformation.SESSION_STATE_ANONYMOUS);
        }

        return sessionInformation;
    }

    @GetMapping("{providerKey}/login")
    public void login(
            @PathVariable(value = "providerKey") String providerKey,
            ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        // Load login implementation
        LoginDriver loginDriver = loadLoginDriver(providerKey);
        LoginDriverResult loginDriverResult = loginDriver.startLogin();

        // Store login state
        String returnUrl = loadLoginReturnUrl(request);
        storeLoginState(providerKey, loginDriverResult, returnUrl, response);

        // Redirect the user
        response.getHeaders().add("Location", loginDriverResult.getAuthURI().toString());
        response.setRawStatusCode(302);
    }

    public LoginDriver loadLoginDriver(String providerKey) {

        // Load settings
        LoginProvider provider = loadProvider(providerKey);
        URI callbackURI = loadCallbackURI(providerKey);
        String driverName = provider.getType();

        try {
            return loginDriverLoader.loadDriverByKey(driverName, callbackURI, provider.getWith());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "could not find login driver", e);
        }
    }

    public String loadLoginReturnUrl(ServerHttpRequest request) {

        String returnUrl = request.getQueryParams().getFirst("returnUrl");

        // Validate return url
        if (returnUrl == null) {
            // If no return url is specified in the request, we use the default return url
            return config.getSessionBehaviour().getRedirectLoginSuccess();
        }

        if (!isValidReturnUrl(returnUrl))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");

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
        allowedHosts.add(config.getHostUri());
        return UrlUtils.isValidReturnUrl(returnUrl, allowedHosts.toArray(new String[]{}));
    }

    @GetMapping("{providerKey}/callback")
    public void callback(
            @PathVariable(value = "providerKey") String providerKey,
            ServerWebExchange exchange) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();

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
            response.getHeaders().add("Location", loginState.getReturnUrl());
            response.setRawStatusCode(302);

        } catch (AuthenticationException e) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

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

    private void createSession(String providerKey, UserModel model, ServerHttpResponse response) {

        var filterContext = new HashMap<String, Object>();

        filterContext.put("providerKey", providerKey);
        filterContext.put("userModel", model);

        List<NellySessionFilter> sessionFilters = getNellySessionFilters();

        sessionFilters.forEach(f -> f.createSession(filterContext, response));
    }

    private List<NellySessionFilter> getNellySessionFilters() {

        return NellySessionFilter.getNellySessionFilters(context);
    }

    @GetMapping("logout")
    public void logout(
            ServerWebExchange exchange) {

        var request = exchange.getRequest();
        var response = exchange.getResponse();

        // Logout csrf prevention
        CsrfProtectionValidation csrfValidation = getCsrfValidationMethod();
        if (csrfValidation.shouldBlockRequest(exchange)) {
            log.info("Blocked logout request due to csrf protection");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            // Destroy the user session
            destroySession(response);

            // Get redirection url
            String returnUrl = loadLogoutReturnUrl(request);

            // Redirect the user
            response.getHeaders().add("Location", returnUrl);
            response.setRawStatusCode(302);
        }
    }

    private CsrfProtectionValidation getCsrfValidationMethod() {

        return CsrfProtectionValidation.loadValidationImplementation(
                CsrfSamesiteStrictValidation.NAME, context);
    }

    private void destroySession(ServerHttpResponse response) {

        var filterContext = new HashMap<String, Object>();
        List<NellySessionFilter> sessionFilters = getNellySessionFilters();
        sessionFilters.forEach(f -> f.destroySession(filterContext, response));
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
