package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import ch.gianlucafrei.nellygateway.config.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.AuthenticationException;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.UserModel;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginDriver {

    LoginState getRedirectUri();
    UserModel processCallback(HttpServletRequest request, LoginState state) throws AuthenticationException;

    List<String> getSettingsErrors(LoginProviderSettings settings);
    default boolean settingsAreValid(LoginProviderSettings settings){
        return getSettingsErrors(settings).isEmpty();
    }
}
