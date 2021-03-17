package org.owasp.oag.exception;

/**
 * Used for all cases where configuration is bad.
 */
public class ConfigurationException extends SystemException {

    public ConfigurationException(String message, Throwable parentException) {
        super(message, parentException);
    }

}
