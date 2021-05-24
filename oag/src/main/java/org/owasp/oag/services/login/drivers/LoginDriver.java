package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.session.UserModel;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;
import java.net.URL;
import java.util.List;

public interface LoginDriver {

    LoginDriverResult startLogin(URI callbackUri);

    UserModel processCallback(ServerHttpRequest request, String state, URI callbackUri) throws AuthenticationException;

    /**
     * This method is called when the user log out.
     * If desired, the login provider should logout the user from the underlying user federation (i.e. OIDC provider)
     * If the method return a URL, the user will be redirected to this url after the OAG session has been destroyed.
     * This is useful for redirection based federated logut.
     * If the method returns null, the user will be redirected to the default redirectLogout from the configuration file.
     *
     * @param userModel User model of the OAG session (Was returned by processCallback(...))
     * @return null or url to redirect the user to
     */
    URL processFederatedLogout(UserModel userModel);

    default boolean settingsAreValid(LoginProviderSettings settings) {
        return getSettingsErrors(settings).isEmpty();
    }

    List<String> getSettingsErrors(LoginProviderSettings settings);
}
