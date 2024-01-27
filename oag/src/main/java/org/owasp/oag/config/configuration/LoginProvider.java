package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.infrastructure.factories.LoginDriverFactory;
import org.owasp.oag.services.login.drivers.InvalidProviderSettingsException;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class LoginProvider implements ErrorValidation {

    private String type;
    private LoginProviderSettings with;

    public LoginProvider() {
    }

    public LoginProvider(String type, LoginProviderSettings with) {
        this.type = type;
        this.with = with;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LoginProviderSettings getWith() {
        return with;
    }

    public void setWith(LoginProviderSettings with) {
        this.with = with;
    }


    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new ArrayList<String>();

        if (type == null)
            errors.add("LoginProvider: No type defined");
        if (with == null)
            errors.add("LoginProvider: No settings defined");

        if (!errors.isEmpty())
            return errors;

        // Check if we can load the driver
        LoginDriverFactory factory = LoginDriverFactory.get(context);
        try {
            LoginDriver loginDriver = factory.loadDriverByKey(type, with);
        } catch (InvalidProviderSettingsException e) {
            var settingErrors = e.getSettingErrors();
            errors.addAll(settingErrors);
        } catch (Exception e) {
            errors.add("LoginDriver: Could not load driver implementation for type '" + type + "'");
        }

        return errors;
    }
}
