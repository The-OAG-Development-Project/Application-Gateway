package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.session.UserModel;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Interface for login driver implementations that handle authentication with external identity providers.
 * Login drivers are responsible for initiating authentication flows, processing callbacks,
 * and handling logout operations.
 */
public interface LoginDriver {

    /**
     * Starts the login process with the identity provider.
     * This typically involves generating an authorization URL and state token.
     *
     * @param callbackUri The URI to which the identity provider should redirect after authentication
     * @return A LoginDriverResult containing the redirect URL and any other necessary information
     */
    LoginDriverResult startLogin(URI callbackUri);

    /**
     * Processes the callback from the identity provider after user authentication.
     * This typically involves exchanging authorization codes for tokens and extracting user information.
     *
     * @param request The HTTP request containing callback parameters
     * @param state The state token to verify the callback is valid
     * @param callbackUri The callback URI that was used in the authentication request
     * @return A UserModel containing authenticated user information
     * @throws AuthenticationException if authentication fails
     */
    UserModel processCallback(ServerHttpRequest request, String state, URI callbackUri) throws AuthenticationException;

    /**
     * This method is called when the user logs out.
     * If desired, the login provider should logout the user from the underlying user federation (i.e. OIDC provider)
     * If the method return a URL, the user will be redirected to this url after the OAG session has been destroyed.
     * This is useful for redirection based federated logout.
     * If the method returns null, the user will be redirected to the default redirectLogout from the configuration file.
     *
     * @param userModel User model of the OAG session (Was returned by processCallback(...))
     * @return null or url to redirect the user to
     */
    URL processFederatedLogout(UserModel userModel);

    /**
     * Checks if the provided settings are valid for this login driver.
     *
     * @param settings The login provider settings to validate
     * @return true if the settings are valid, false otherwise
     */
    default boolean settingsAreValid(LoginProviderSettings settings) {
        return getSettingsErrors(settings).isEmpty();
    }

    /**
     * Gets a list of error messages for any invalid settings.
     *
     * @param settings The login provider settings to validate
     * @return A list of error messages, empty if the settings are valid
     */
    List<String> getSettingsErrors(LoginProviderSettings settings);
}
