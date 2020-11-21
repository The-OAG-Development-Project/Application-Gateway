package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import ch.gianlucafrei.nellygateway.config.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.UserModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GitHubDriver extends Oauth2Driver {

    public GitHubDriver(String providerKey) {
        super(providerKey);
    }

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = super.getSettingsErrors(settings);

        if(errors.isEmpty())
        {
            try {
                String[] scopes = (String[]) settings.get("scopes");

                if (Arrays.stream(scopes).anyMatch(s -> "user".equals(s)))
                    errors.add("'user' be within scope");

                if (Arrays.stream(scopes).anyMatch(s -> "email".equals(s)))
                    errors.add("'email' be within scope");

            } catch (Exception e) {
                errors.add("scopes has invalid format");
            }
        }
    }

    @Override
    protected UserModel loadUserInfo(AccessToken accessToken, LoginState state, LoginProviderSettings settings){

        return null;
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
