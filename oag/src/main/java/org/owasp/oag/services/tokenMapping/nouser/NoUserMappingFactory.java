package org.owasp.oag.services.tokenMapping.nouser;

import org.owasp.oag.services.tokenMapping.UserMapper;
import org.owasp.oag.services.tokenMapping.UserMappingFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating NoUserMapper instances.
 * This factory creates user mappers that do not require any user authentication.
 */
@Component
public class NoUserMappingFactory implements UserMappingFactory {

    /**
     * Creates a new NoUserMapper instance with the specified settings.
     *
     * @param settings Configuration settings for the user mapper
     * @return A new NoUserMapper instance
     */
    @Override
    public UserMapper load(Map<String, Object> settings) {

        return new NoUserMapper();
    }
}
