package org.owasp.oag.services.login.drivers;

import org.owasp.oag.config.configuration.ConfigValidator;
import org.owasp.oag.config.configuration.LoginProvider;
import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates every login provider: its {@code type} is a registered {@link LoginDriverFactory}, and a login
 * driver can be created from its settings.
 */
@Component
public class LoginProviderValidator implements ConfigValidator {

    private final LoginDriverRegistry registry;

    /**
     * Creates the validator.
     *
     * @param registry The login driver registry
     */
    public LoginProviderValidator(LoginDriverRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> validate(MainConfig config) {
        var errors = new ArrayList<String>();
        config.getLoginProviders().forEach((name, provider) -> errors.addAll(validate(name, provider)));
        return errors;
    }

    private List<String> validate(String name, LoginProvider provider) {
        var entry = "LoginProvider '" + name + "'";
        var unknownType = registry.unknownTypeError(entry, provider.getType());
        if (unknownType.isPresent())
            return List.of(unknownType.get());

        try {
            registry.load(provider.getType(), provider.getWith());
            return List.of();
        } catch (InvalidProviderSettingsException e) {
            return e.getSettingErrors();
        } catch (RuntimeException e) {
            return List.of(entry + ": Could not create driver of type '" + provider.getType() + "': " + e.getMessage());
        }
    }
}
