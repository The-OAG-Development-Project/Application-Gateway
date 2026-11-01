package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.extension.TypedExtension;

import java.util.Map;

/**
 * A user mapping factory is responsible for creating a user mapper instance given a map of settings
 * from the configuration file.
 * <p>
 * The factory is selected through the security profile's {@code userMapping.type} value, see {@link #type()}.
 */
public interface UserMappingFactory extends TypedExtension {

    /**
     * Must return a instance of a user mapper class.
     *
     * @param settings A map of settings from the configuration file
     * @return A user mapper object
     */
    UserMapper load(Map<String, Object> settings);
}
