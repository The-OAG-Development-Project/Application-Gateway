package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.context.ApplicationContext;

public interface LoginDriverFactory {
    LoginDriver loadDriverByKey(String driverName, LoginProviderSettings settings);

    static LoginDriverFactory get(ApplicationContext context){

        return context.getBean(LoginDriverFactory.class);
    }
}
