package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.config.InvalidOAGSettingsException;

import java.util.Map;

/**
 * A user mapping factory is responsible for creating a user mapper instance given a map of settings
 * from the configuration file.
 *
 * To add a new type of user mapping you need to implement this class and provide a bean with the
 * name of the mapping + USER_MAPPER_TYPE_POSTFIX.
 */
public interface UserMappingFactory {

    /**
     * Name postfix for beans of this type.
     */
    final static String USER_MAPPER_TYPE_POSTFIX = "-userMapping-factory";

    /**
     * Must return a instance of a user mapper class.
     * @param settings A map of settings from the configuration file
     * @return A user mapper object
     */
    UserMapper load(Map<String, Object> settings) throws InvalidOAGSettingsException;
}
