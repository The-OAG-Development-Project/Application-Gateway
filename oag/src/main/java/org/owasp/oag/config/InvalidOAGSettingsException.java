package org.owasp.oag.config;

public class InvalidOAGSettingsException extends Exception{

    public InvalidOAGSettingsException(String message) {
        super(message);
    }

    public InvalidOAGSettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
