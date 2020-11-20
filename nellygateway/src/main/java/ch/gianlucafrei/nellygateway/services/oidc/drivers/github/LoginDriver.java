package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import ch.gianlucafrei.nellygateway.config.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.UserModel;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginDriver {

    LoginState getRedirectUri(LoginProviderSettings settings);
    UserModel processCallback(HttpServletRequest request, LoginState state, LoginProviderSettings settings);

    List<String> getSettingsErrors(LoginProviderSettings settings);
    default boolean settingsAreValid(LoginProviderSettings settings){
        return getSettingsErrors(settings).isEmpty();
    }
}
