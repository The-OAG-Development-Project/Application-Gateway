package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import ch.gianlucafrei.nellygateway.config.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitHubDriver extends LoginDriverBase{

    public GitHubDriver(String providerKey) {
        super(providerKey);
    }

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = new ArrayList<String>();

        if(! settings.containsKey("clientId"))
            errors.add("ClinetId missing");

        if(! settings.containsKey("clientSecret"))
            errors.add("ClinetId missing");

        if(! settings.containsKey("scopes")){
            errors.add("Scopes missing");
        }
        else {

            try{
                String[] scopes = (String[]) settings.get("scopes");

                if(Arrays.stream(scopes).anyMatch(s -> "user".equals(s)))
                    errors.add("'user' be within scope");

                if(Arrays.stream(scopes).anyMatch(s -> "email".equals(s)))
                    errors.add("'email' be within scope");
            }
            catch (Exception e) {
                errors.add("scopes has invalid format");
            }
        }

        return errors;
    }

    @Override
    public LoginState getRedirectUri(LoginProviderSettings settings) {

        return null;
    }

    @Override
    public UserModel processCallback(HttpServletRequest request, LoginState state, LoginProviderSettings settings) {
        return null;
    }
}
