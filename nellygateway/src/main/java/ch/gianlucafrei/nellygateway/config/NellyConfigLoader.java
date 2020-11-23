package ch.gianlucafrei.nellygateway.config;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;

import java.io.IOException;

public interface NellyConfigLoader {
    NellyConfig loadConfiguration() throws IOException;
}
