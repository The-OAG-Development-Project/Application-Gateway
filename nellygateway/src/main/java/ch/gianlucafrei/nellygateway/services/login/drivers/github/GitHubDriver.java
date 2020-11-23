package ch.gianlucafrei.nellygateway.services.login.drivers.github;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.login.drivers.AuthenticationException;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import ch.gianlucafrei.nellygateway.services.login.drivers.oauth.Oauth2Driver;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class GitHubDriver extends Oauth2Driver {

    public GitHubDriver(LoginProviderSettings settings, URI callbackURI) {
        super(settings, callbackURI);
    }

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = super.getSettingsErrors(settings);

        if(errors.isEmpty())
        {
            Scope scopes = getScopes(settings);

            if (! scopes.contains("user"))
                errors.add("'user' be within scope");

            if (! scopes.contains("email"))
                errors.add("'email' be within scope");
        }

        return errors;
    }

    @Override
    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {
        return super.loadTokens(clientAuth, tokenEndpoint, codeGrant);
    }

    @Override
    protected UserModel loadUserInfo(Tokens tokens){

        AccessToken accessToken = tokens.getAccessToken();
        RefreshToken refreshToken = tokens.getRefreshToken();

        try{
            // Load data
            String email = loadUserEmail(accessToken);
            GitHubUserResponse profileResponse = makeGitHubApiRequest("https://api.github.com/user", accessToken.getValue(), GitHubUserResponse.class);

            // Create user model
            UserModel model = new UserModel(profileResponse.getId());
            model.set("email", email);
            model.set("picture", profileResponse.getAvatar_url());
            model.set("github-username", profileResponse.getLogin());
            model.set("access-token", accessToken.toString());

            model.set("refreshToken", refreshToken != null ? refreshToken.toString() : null);

            return model;
        }
        catch (IOException | InterruptedException ex){
            throw new RuntimeException("Could not load user profile data", ex);
        }
    }

    protected String loadUserEmail(AccessToken accessToken){

        try {

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
