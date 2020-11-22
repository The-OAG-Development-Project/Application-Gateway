package ch.gianlucafrei.nellygateway.services.login.drivers.github;

import ch.gianlucafrei.nellygateway.config.AuthProvider;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCCallbackResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCLoginStepResult;
import ch.gianlucafrei.nellygateway.services.oidc.OIDCService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class GitHubLoginImplementation extends OIDCService {

    private static Logger log = LoggerFactory.getLogger(GitHubLoginImplementation.class);

    @Override
    public OIDCLoginStepResult getRedirectUri(AuthProvider providerSettings, String callbackUri) {

        // The authorisation endpoint of the server
        URI authzEndpoint = providerSettings.getAuthEndpointAsURI();

        // The client identifier provisioned by the server
        ClientID clientID = new ClientID(providerSettings.getClientId());

        // The requested scope values for the token
        Scope scope = new Scope(providerSettings.getScopes());

        // The client callback URI, typically pre-registered with the server
        URI callback = getCallbackUri(callbackUri);

        // Generate random state string for pairing the response to the request
        State state = new State();

        // Build the request
        AuthorizationRequest request = new AuthorizationRequest.Builder(
                new ResponseType(ResponseType.Value.CODE), clientID)
                .scope(scope)
                .state(state)
                .redirectionURI(callback)
                .endpointURI(authzEndpoint)
                .build();

        // Use this URI to send the end-user's browser to the server
        URI requestURI = request.toURI();

        OIDCLoginStepResult result = new OIDCLoginStepResult();
        result.state = state.toString();
        result.redirectUri = requestURI.toString();
        return result;
    }

    @Override
    public OIDCCallbackResult processCallback(AuthProvider providerSettings, String codeStr, String callbackUri) {

        // Load user token
        AuthorizationCode code = new AuthorizationCode(codeStr);
        ClientAuthentication clientAuth = getClientAuthentication(providerSettings);
        URI tokenEndpoint = providerSettings.getTokenEndpointAsURI();
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, getCallbackUri(callbackUri));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        TokenResponse response = sendTokenRequest(request);

        OIDCCallbackResult result = new OIDCCallbackResult();
        if (!response.indicatesSuccess()) {

            // We got an error response...
            TokenErrorResponse errorResponse = response.toErrorResponse();
            result.success = false;

            log.info(String.format("Unsuccessful token response: %s", errorResponse.getErrorObject().getDescription()));
            return result;
        }

        // Load user Email
        AccessToken accessToken = response.toSuccessResponse().getTokens().getAccessToken();

        String email = loadUserEmail(accessToken);
        result.success = true;
        result.subject = email;

        return result;
    }

    protected String loadUserEmail(AccessToken accessToken){

        try {
            GitHubUserResponse profileResponse = makeGitHubApiRequest("https://api.github.com/user", accessToken.getValue(), GitHubUserResponse.class);
            GitHubEmailsResponse emailsResponse = makeGitHubApiRequest("https://api.github.com/user/emails", accessToken.getValue(), GitHubEmailsResponse.class);

            Optional<GitHubUserEmail> anyEmail = emailsResponse.stream()
                    .filter(e -> e.isVerified())
                    .filter(e -> e.isPrimary())
                    .findAny();


            if(anyEmail.isPresent())
                return anyEmail.get().getEmail();
            else
                return null;

        } catch (Exception e) {
            throw new RuntimeException("Could not load user profile info", e);
        }
    }

    protected <T> T makeGitHubApiRequest(String endpoint, String accessToken, Class<T> clazz) throws IOException, InterruptedException {

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
                URI.create(endpoint))
                .header("accept", "application/json")
                .headers("Authorization", "token " + accessToken)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        ObjectMapper om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om.readValue(body, clazz);
    }
}
