package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.AuthenticationException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;
import java.util.List;

public interface LoginDriver {

    LoginDriverResult startLogin(URI callbackUri);

    UserModel processCallback(ServerHttpRequest request, String state, URI callbackUri) throws AuthenticationException;

    default boolean settingsAreValid(LoginProviderSettings settings) {
        return getSettingsErrors(settings).isEmpty();
    }

    List<String> getSettingsErrors(LoginProviderSettings settings);
}
