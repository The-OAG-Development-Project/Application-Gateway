package ch.gianlucafrei.nellygateway.services.login.drivers;

import ch.gianlucafrei.nellygateway.config.configuration.LoginProviderSettings;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginDriver {

    LoginDriverResult startLogin();
    UserModel processCallback(HttpServletRequest request, String state) throws AuthenticationException;

    List<String> getSettingsErrors(LoginProviderSettings settings);
    default boolean settingsAreValid(LoginProviderSettings settings){
        return getSettingsErrors(settings).isEmpty();
    }
}
