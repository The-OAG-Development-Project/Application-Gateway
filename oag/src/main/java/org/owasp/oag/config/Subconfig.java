package org.owasp.oag.config;

import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.context.ApplicationContext;

import java.util.List;

public interface Subconfig {

    List<String> getErrors(ApplicationContext context, MainConfig rootConfig);
}
