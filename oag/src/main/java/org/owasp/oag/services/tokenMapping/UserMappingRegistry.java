package org.owasp.oag.services.tokenMapping;

import org.owasp.oag.extension.TypeRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registry of all {@link UserMappingFactory} implementations, keyed by user mapping type.
 */
@Component
public class UserMappingRegistry extends TypeRegistry<UserMappingFactory> {

    /**
     * Creates the registry.
     *
     * @param factories All user mapping factories
     */
    public UserMappingRegistry(List<UserMappingFactory> factories) {
        super("user mapping", factories);
    }
}
