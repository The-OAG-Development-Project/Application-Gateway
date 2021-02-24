package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

public interface LoginDriver {

    LoginDriverResult startLogin();

    UserModel processCallback(ServerHttpRequest request, String state) throws AuthenticationException;

    default boolean settingsAreValid(LoginProviderSettings settings) {
        return getSettingsErrors(settings).isEmpty();
    }

    List<String> getSettingsErrors(LoginProviderSettings settings);
}
