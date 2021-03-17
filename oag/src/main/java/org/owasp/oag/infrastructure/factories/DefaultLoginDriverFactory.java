package org.owasp.oag.infrastructure.factories;

import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.login.drivers.LoginDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoginDriverFactory implements LoginDriverFactory {

    private final ApplicationContext context;

    public DefaultLoginDriverFactory(@Autowired ApplicationContext context) {
        this.context = context;
    }

    @Override
    public LoginDriver loadDriverByKey(String driverName, LoginProviderSettings settings) {

        try{
            var driverFactory = context.getBean(driverName + "-driver-factory",
                    org.owasp.oag.services.login.drivers.LoginDriverFactory.class);

            return driverFactory.load(settings);
        }
        catch (Exception ex){
            throw new ConfigurationException("Login driver factory with name " + driverName + " not found", ex);
        }
    }
}
