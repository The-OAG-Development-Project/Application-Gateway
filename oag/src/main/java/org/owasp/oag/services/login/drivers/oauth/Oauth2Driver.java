package org.owasp.oag.services.login.drivers.oauth;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.login.drivers.LoginDriverBase;
import org.owasp.oag.services.login.drivers.LoginDriverResult;
import org.owasp.oag.session.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for OAuth2 login drivers.
 * Provides common functionality for handling OAuth2 login flows.
 */
public abstract class Oauth2Driver extends LoginDriverBase {

    /**
     * Constructor for Oauth2Driver.
     *
     * @param settings The login provider settings.
     */
    public Oauth2Driver(LoginProviderSettings settings) {
        super(settings);
    }

    /**
     * Starts the login process by generating an authorization request URI.
     *
     * @param callbackUri The callback URI to redirect to after login.
     * @return A {@link LoginDriverResult} containing the authorization request URI and state.
     */
    @Override
    public LoginDriverResult startLogin(URI callbackUri) {
        var settings = getSettings();

        // Prepare OAuth2 request
        URI authzEndpoint = getAuthEndpoint(settings);
        ClientID clientID = getClientId(settings);
        Scope scope = getScopes(settings);

        // Generate random state string for pairing the response to the request
        State state = new State();

        // Build the request
        AuthorizationRequest request = new AuthorizationRequest.Builder(
                new ResponseType(ResponseType.Value.CODE), clientID)
                .scope(scope)
                .state(state)
                .redirectionURI(callbackUri)
                .endpointURI(authzEndpoint)
                .build();

        // Use this URI to send the end-user's browser to the server
        URI requestURI = request.toURI();
        return new LoginDriverResult(requestURI, state.toString());
    }

    /**
     * Retrieves the authorization endpoint URI from the settings.
     *
     * @param settings The login provider settings.
     * @return The authorization endpoint URI.
     */
    protected URI getAuthEndpoint(LoginProviderSettings settings) {
        try {
            return new URI((String) settings.get("authEndpoint"));
        } catch (Exception e) {
            throw new ConfigurationException("Invalid auth endpoint", null);
        }
    }

    /**
     * Retrieves the client ID from the settings.
     *
     * @param settings The login provider settings.
     * @return The client ID.
     */
    protected ClientID getClientId(LoginProviderSettings settings) {
        try {
            return new ClientID((String) settings.get("clientId"));
        } catch (Exception e) {
            throw new ConfigurationException("Invalid clientId", null);
        }
    }

    /**
     * Retrieves the scopes from the settings.
     *
     * @param settings The login provider settings.
     * @return The scopes.
     */
    protected Scope getScopes(LoginProviderSettings settings) {
        try {
            Object scopes = settings.get("scopes");

            if (scopes instanceof String[])
                return new Scope((String[]) scopes);

            @SuppressWarnings("unchecked") List<String> scopesList = (List<String>) scopes;
            return new Scope(scopesList.toArray(new String[]{}));

        } catch (Exception e) {
            throw new ConfigurationException("Invalid scope", null);
        }
    }

    /**
     * Processes the callback from the OAuth2 server.
     *
     * @param request             The server HTTP request.
     * @param stateFromLoginStep  The state from the login step.
     * @param callbackUri         The callback URI.
     * @return A {@link UserModel} representing the authenticated user.
     * @throws AuthenticationException If authentication fails.
     */
    @Override
    public UserModel processCallback(ServerHttpRequest request, String stateFromLoginStep, URI callbackUri) throws AuthenticationException {
        var settings = getSettings();

        String authCode = request.getQueryParams().getFirst("code");
        if (authCode == null)
            throw new AuthenticationException("No auth code");

        String stateFromRequest = request.getQueryParams().getFirst("state");
        if (stateFromRequest == null)
            throw new AuthenticationException("No state");

        if (!stateFromLoginStep.equals(stateFromRequest))
            throw new AuthenticationException("State mismatch");

        AuthorizationCode code = new AuthorizationCode(authCode);

        ClientAuthentication clientAuth = new ClientSecretBasic(
                getClientId(settings),
                getClientSecret(settings));

        URI tokenEndpoint = getTokenEndpoint(settings);
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callbackUri);

        Tokens tokens = loadTokens(clientAuth, tokenEndpoint, codeGrant);

        // Load user Email
        return loadUserInfo(tokens);
    }

    /**
     * Validates the settings and returns a list of errors.
     *
     * @param settings The login provider settings.
     * @return A list of error messages.
     */
    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {
        var errors = new ArrayList<String>();

        if (!settings.containsKey("clientId"))
            errors.add("ClientId missing");

        if (!settings.containsKey("clientSecret"))
            errors.add("ClientSecret missing");

        if (!settings.containsKey("scopes"))
            errors.add("Scopes missing");

        if (!settings.containsKey("tokenEndpoint"))
            errors.add("tokenEndpoint missing");

        if (!settings.containsKey("authEndpoint"))
            errors.add("auth endpoint missing");

        if (settings.containsKey("federatedLogoutUrl")) {
            var federatedLogoutUrl = settings.get("federatedLogoutUrl");

            if (!(federatedLogoutUrl instanceof String))
                errors.add("federatedLogoutUrl must be a valid url");
            else {
                try {
                    new URL((String) federatedLogoutUrl);
                } catch (MalformedURLException e) {
                    errors.add("federatedLogoutUrl must be a valid url");
                }
            }
        }

        return errors;
    }

    /**
     * Retrieves the client secret from the settings.
     *
     * @param settings The login provider settings.
     * @return The client secret.
     */
    protected Secret getClientSecret(LoginProviderSettings settings) {
        try {
            return new Secret((String) settings.get("clientSecret"));
        } catch (Exception e) {
            throw new ConfigurationException("Invalid clientSecret", null);
        }
    }

    /**
     * Retrieves the token endpoint URI from the settings.
     *
     * @param settings The login provider settings.
     * @return The token endpoint URI.
     */
    protected URI getTokenEndpoint(LoginProviderSettings settings) {
        try {
            return new URI((String) settings.get("tokenEndpoint"));
        } catch (Exception e) {
            throw new ConfigurationException("Invalid token endpoint", null);
        }
    }

    /**
     * Loads tokens from the token endpoint.
     *
     * @param clientAuth   The client authentication.
     * @param tokenEndpoint The token endpoint URI.
     * @param codeGrant    The authorization grant.
     * @return The tokens.
     * @throws AuthenticationException If token loading fails.
     */
    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {
        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, null);
        TokenResponse tokenResponse = sendTokenRequest(tokenRequest);

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            String message = errorResponse.getErrorObject().getDescription();
            throw new AuthenticationException(message);
        }

        return tokenResponse.toSuccessResponse().getTokens();
    }

    /**
     * Abstract method to load user information from tokens.
     *
     * @param accessToken The access token.
     * @return A {@link UserModel} representing the user.
     */
    protected abstract UserModel loadUserInfo(Tokens accessToken);

    /**
     * Sends a token request to the token endpoint.
     *
     * @param tokenRequest The token request.
     * @return The token response.
     */
    protected TokenResponse sendTokenRequest(TokenRequest tokenRequest) {
        TokenResponse tokenResponse;
        try {
            HTTPRequest tokenHttpRequest = tokenRequest.toHTTPRequest();
            tokenHttpRequest.setAccept("application/json");
            HTTPResponse tokenHttpResponse = tokenHttpRequest.send();
            tokenResponse = OIDCTokenResponseParser.parse(tokenHttpResponse);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response IO error");
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response parse error");
        }
        return tokenResponse;
    }

    /**
     * Processes federated logout for the user.
     *
     * @param userModel The user model.
     * @return The federated logout URL, or null if not configured.
     */
    @Override
    public URL processFederatedLogout(UserModel userModel) {
        String federatedLogoutUrl = (String) getSettings().getOrDefault("federatedLogoutUrl", null);

        if (federatedLogoutUrl == null)
            return null;

        try {
            return new URL(federatedLogoutUrl);
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Invalid federatedLogoutUrl, should have been verified in getSettingsErrors");
        }
    }
}
