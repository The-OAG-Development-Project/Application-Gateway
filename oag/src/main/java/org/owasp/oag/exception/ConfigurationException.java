package org.owasp.oag.exception;

/**
 * Used for all cases where configuration is bad.
 */
public class ConfigurationException extends SystemException {

    /**
     * Creates a new ConfigurationException with the given message and parent exception.
     *
     * @param message         The message to log.
     * @param parentException The parent exception if available.
     */
    public ConfigurationException(String message, Throwable parentException) {
        super(message, parentException);
    }

    /**
     * Creates a new ConfigurationException with the given message.
     *
     * @param message The message to log.
     */
    public ConfigurationException(String message) {
        super(message);
    }

}
