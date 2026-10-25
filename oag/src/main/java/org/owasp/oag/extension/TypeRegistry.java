package org.owasp.oag.extension;

import org.owasp.oag.exception.ConfigurationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Maps the configuration {@code type} values of one extension point to their implementations.
 * Concrete registries are Spring components that receive all extensions of their point as a list.
 *
 * @param <T> The extension type of the extension point
 */
public abstract class TypeRegistry<T extends TypedExtension> {

    private final String extensionName;
    private final Map<String, T> extensions = new TreeMap<>();

    /**
     * Creates a registry from the given extensions.
     *
     * @param extensionName Human-readable name of the extension point, used in error messages
     * @param extensions    All extensions of the extension point
     * @throws ConfigurationException If two extensions declare the same type
     */
    protected TypeRegistry(String extensionName, List<? extends T> extensions) {
        this.extensionName = extensionName;
        for (T extension : extensions) {
            T existing = this.extensions.putIfAbsent(extension.type(), extension);
            if (existing != null) {
                throw new ConfigurationException("Duplicate " + extensionName + " type '" + extension.type() + "': "
                        + existing.getClass().getName() + " and " + extension.getClass().getName());
            }
        }
    }

    /**
     * Returns the extension registered for the given type.
     *
     * @param type The configured type
     * @return The extension for the type
     * @throws ConfigurationException If no extension is registered for the type
     */
    public T get(String type) {
        if (!contains(type)) {
            throw new ConfigurationException("No " + extensionName + " for type '" + type + "'. Known types: " + types());
        }
        return extensions.get(type);
    }

    /**
     * Checks whether an extension is registered for the given type.
     *
     * @param type The configured type, may be {@code null}
     * @return {@code true} if an extension is registered for the type
     */
    public boolean contains(String type) {
        return type != null && extensions.containsKey(type);
    }

    /**
     * Returns all registered types in alphabetical order.
     *
     * @return The registered types
     */
    public Set<String> types() {
        return Collections.unmodifiableSet(extensions.keySet());
    }
}
