package org.owasp.oag.services.login.drivers.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.services.login.drivers.oauth.Oauth2Driver;
import org.owasp.oag.session.UserModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class GithubDriver extends Oauth2Driver {

    public GithubDriver(LoginProviderSettings settings) {
        super(settings);
    }

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = super.getSettingsErrors(settings);

        if (errors.isEmpty()) {
            Scope scopes = getScopes(settings);

            if (!scopes.contains("user"))
                errors.add("'user' be within scope");

            if (!scopes.contains("email"))
                errors.add("'email' be within scope");
        }

        return errors;
    }

    @Override
    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {
        return super.loadTokens(clientAuth, tokenEndpoint, codeGrant);
    }

    @Override
    protected UserModel loadUserInfo(Tokens tokens) {

        AccessToken accessToken = tokens.getAccessToken();
        RefreshToken refreshToken = tokens.getRefreshToken();

        try {
            // Load data
            String email = loadUserEmail(accessToken);
            GithubUserResponse profileResponse = makeGitHubApiRequest("https://api.github.com/user", accessToken.getValue(), GithubUserResponse.class);

            // Create user model
            UserModel model = new UserModel(profileResponse.id);
            model.set("email", email);
            model.set("picture", profileResponse.avatar_url);
            model.set("preferred_username", profileResponse.login);
            model.set("email_verified", "true");
            model.set("sub", model.getId());
            model.set("name", profileResponse.name);
            model.set("profile", profileResponse.url);
            model.set("updated_at", profileResponse.updated_at);
            model.set("created_at", profileResponse.created_at);

            model.set("access-token", accessToken.toString());
            model.set("refreshToken", refreshToken != null ? refreshToken.toString() : null);

            return model;
        } catch (IOException | InterruptedException ex) {
            throw new ApplicationException("Could not load user profile data", ex);
        }
    }

    protected String loadUserEmail(AccessToken accessToken) {

        try {

            GithubEmailsResponse emailsResponse = makeGitHubApiRequest("https://api.github.com/user/emails", accessToken.getValue(), GithubEmailsResponse.class);

            Optional<GithubUserEmail> anyEmail = emailsResponse.stream()
                    .filter(GithubUserEmail::isVerified)
                    .filter(GithubUserEmail::isPrimary)
                    .findAny();


            return anyEmail.map(GithubUserEmail::getEmail).orElse(null);

        } catch (Exception e) {
            throw new ApplicationException("Could not load user profile info", e);
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
