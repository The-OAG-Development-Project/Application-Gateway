package org.owasp.oag.config;

import org.owasp.oag.config.configuration.MainConfig;

import java.io.IOException;

public interface ConfigLoader {
    MainConfig loadConfiguration() throws IOException;
}
