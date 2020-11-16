package ch.gianlucafrei.nellygateway.services.oidc;

import ch.gianlucafrei.nellygateway.config.AuthProvider;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OIDCService {

    private static Logger log = LoggerFactory.getLogger(OIDCService.class);

    public OIDCLoginStepResult getRedirectUri(AuthProvider providerSettings, String callbackUri) {
        OIDCLoginStepResult result = new OIDCLoginStepResult();

        // The client ID provisioned by the OpenID provider when
        // the client was registered
        ClientID clientID = new ClientID(providerSettings.getClientId());

        // The client callback URL
        URI callback = null;
        try {
            callback = new URI(callbackUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Callback URL");
        }

        // Generate random state string to securely pair the callback to this request
        State state = new State();
        Nonce nonce = new Nonce();

        // Compose the OpenID authentication request (for the code flow)
        AuthenticationRequest request = null;
        try {

            Scope scope = new Scope(providerSettings.getScopes());
            ResponseType responseType = new ResponseType("code");
            URI authEndpoint = new URI(providerSettings.getAuthEndpoint());

            request = new AuthenticationRequest.Builder(
                    responseType,
                    scope,
                    clientID,
                    callback)
                    .endpointURI(authEndpoint)
                    .state(state)
                    .nonce(nonce)
                    .build();

        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Auth Endpoint URI");
        }
        result.redirectUri = request.toURI().toString();
        return result;
    }

    public OIDCCallbackResult processCallback(AuthProvider providerSettings, String codeStr, String callbackUri) {
        OIDCCallbackResult result = new OIDCCallbackResult();
        OIDCLoadTokenResult tokenResult = loadTokens(providerSettings, codeStr, callbackUri);

        if (!tokenResult.success) {
            result.success = false;
            return result;
        }


        // Currently not necessary because we directly communicate with the identity provider
        validateToken(providerSettings, tokenResult.idToken);

        try {
            result.subject = tokenResult.idToken.getJWTClaimsSet().getSubject();
            result.issuer = tokenResult.idToken.getJWTClaimsSet().getIssuer();
            result.originalToken = tokenResult.idToken.getParsedString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        result.success = true;
        log.info(String.format("Loaded id token successfully from issuer %s", result.issuer));
        return result;
    }

    private OIDCLoadTokenResult loadTokens(AuthProvider providerSettings, String codeStr, String callbackUri) {
        OIDCLoadTokenResult result = new OIDCLoadTokenResult();

        // Construct the code grant from the code obtained from the authz endpoint
        // and the original callback URI used at the authz endpoint
        AuthorizationCode code = new AuthorizationCode(codeStr);
        URI callback = null;
        try {
            callback = new URI(callbackUri);
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Provider error"
            );
        }

        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

        // The credentials to authenticate the client at the token endpoint
        ClientID clientID = new ClientID(providerSettings.getClientId());
        Secret clientSecret = new Secret(providerSettings.getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        // The token endpoint
        URI tokenEndpoint = null;
        try {
            tokenEndpoint = new URI(providerSettings.getTokenEndpoint());
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Provider error");
        }

        // Make the token request
        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        TokenResponse tokenResponse;
        HTTPResponse tokenHttpResponse;
        try {
            HTTPRequest tokenHttpRequest = tokenRequest.toHTTPRequest();
            tokenHttpRequest.setAccept("application/json");
            tokenHttpResponse = tokenHttpRequest.send();
            tokenResponse = OIDCTokenResponseParser.parse(tokenHttpResponse);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response io error");
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Token response parse error");
        }

        if (!tokenResponse.indicatesSuccess()) {

            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            result.success = false;
            result.errorMessage = errorResponse.getErrorObject().getDescription();

            log.info(String.format("Unsuccessful token response: %s", result.errorMessage));
            return result;
        }

        log.info("Successfull token response");
        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();

        // Get the ID and access token, the server may also return a refresh token
        result.idToken = successResponse.getOIDCTokens().getIDToken();
        result.accessToken = successResponse.getOIDCTokens().getAccessToken();
        result.refreshToken = successResponse.getOIDCTokens().getRefreshToken();
        result.success = true;
        return result;
    }

    private void validateToken(AuthProvider providerSetting, JWT idToken) {
        // TODO implement
    }
}
