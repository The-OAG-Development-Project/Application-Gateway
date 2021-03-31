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
import org.owasp.oag.services.login.drivers.AuthenticationException;
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

public abstract class Oauth2Driver extends LoginDriverBase {


    public Oauth2Driver(LoginProviderSettings settings) {
        super(settings);
    }

    @Override
    public LoginDriverResult startLogin(URI callbackUri) {

        var settings = getSettings();

        // Preprare Oauth2 request
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

    protected URI getAuthEndpoint(LoginProviderSettings settings) {

        try {
            return new URI((String) settings.get("authEndpoint"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid auth endpoint");
        }
    }

    protected ClientID getClientId(LoginProviderSettings settings) {

        try {
            return new ClientID((String) settings.get("clientId"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid clientId");
        }
    }

    protected Scope getScopes(LoginProviderSettings settings) {

        try {

            Object scopes = settings.get("scopes");

            if (scopes instanceof String[])
                return new Scope((String[]) scopes);

            List<String> scopesList = (List<String>) scopes;
            return new Scope(scopesList.toArray(new String[]{}));

        } catch (Exception e) {
            throw new RuntimeException("Invalid scope");
        }
    }

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
            throw new AuthenticationException("State missmatch");

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

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = new ArrayList<String>();

        if (!settings.containsKey("clientId"))
            errors.add("ClinetId missing");

        if (!settings.containsKey("clientSecret"))
            errors.add("ClinetSecret missing");

        if (!settings.containsKey("scopes"))
            errors.add("Scopes missing");

        if (!settings.containsKey("tokenEndpoint"))
            errors.add("tokenEndpoint missing");

        if (!settings.containsKey("authEndpoint"))
            errors.add("auth endpoint missing");

        if (settings.containsKey("federatedLogoutUrl")){

            var federatedLogoutUrl = settings.get("federatedLogoutUrl");

            if(! (federatedLogoutUrl instanceof String))
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

    protected Secret getClientSecret(LoginProviderSettings settings) {

        try {
            return new Secret((String) settings.get("clientSecret"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid clientId");
        }
    }

    protected URI getTokenEndpoint(LoginProviderSettings settings) {

        try {
            return new URI((String) settings.get("tokenEndpoint"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid token endpoint");
        }
    }

    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {

        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        TokenResponse tokenResponse = sendTokenRequest(tokenRequest);

        if (!tokenResponse.indicatesSuccess()) {

            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            String message = errorResponse.getErrorObject().getDescription();
            throw new AuthenticationException(message);
        }

        var tokens = tokenResponse.toSuccessResponse().getTokens();
        return tokens;
    }

    protected abstract UserModel loadUserInfo(Tokens accessToken);

    protected TokenResponse sendTokenRequest(TokenRequest tokenRequest) {

        TokenResponse tokenResponse;
        try {
            HTTPRequest tokenHttpRequest = tokenRequest.toHTTPRequest();
            tokenHttpRequest.setAccept("application/json");
            HTTPResponse tokenHttpResponse = tokenHttpRequest.send();
            tokenResponse = OIDCTokenResponseParser.parse(tokenHttpResponse);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response io error");
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response parse error");
        }
        return tokenResponse;
    }
    
    @Override
    public URL processFederatedLogout(UserModel userModel) {

        String federatedLogoutUrl = (String) getSettings().getOrDefault("federatedLogoutUrl", null);

        if(federatedLogoutUrl == null)
            return null;

        try {
            return new URL(federatedLogoutUrl);
        } catch (MalformedURLException e) {

            throw new RuntimeException("Invalid federatedLogoutUrl, should have be verified in getSettingsErrors");
        }
    }
}
