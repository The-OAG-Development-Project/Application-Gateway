package org.owasp.oag.extension;

/**
 * An extension that is selected through a {@code type} value in the configuration file.
 * Implementations are registered in the {@link TypeRegistry} of their extension point.
 */
public interface TypedExtension {

    /**
     * Returns the configuration value that selects this extension.
     *
     * @return The type, unique within its extension point
     */
    String type();
}
