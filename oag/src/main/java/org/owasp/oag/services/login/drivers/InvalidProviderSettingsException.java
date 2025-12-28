package org.owasp.oag.services.login.drivers;

import org.owasp.oag.exception.ConfigurationException;

import java.util.List;

/**
 * Exception thrown when invalid settings are provided for an authentication provider.
 * This exception contains a list of specific setting errors that occurred.
 */
public class InvalidProviderSettingsException extends ConfigurationException {

    /**
     * List of specific setting errors that were encountered.
     */
    private List<String> settingErrors;

    /**
     * Constructs a new exception with the specified list of setting errors.
     *
     * @param settingErrors The list of setting errors that were encountered
     */
    public InvalidProviderSettingsException(List<String> settingErrors) {
        super("Invalid provider settings: " + formatErros(settingErrors));
        this.settingErrors = settingErrors;
    }

    /**
     * Formats a list of errors into a comma-separated string.
     *
     * @param errors The list of error messages to format
     * @return A comma-separated string containing all error messages
     */
    public static String formatErros(List<String> errors) {
        return String.join(", ", errors);
    }

    /**
     * Gets the list of setting errors that were encountered.
     *
     * @return The list of setting errors
     */
    public List<String> getSettingErrors() {
        return settingErrors;
    }

    /**
     * Sets the list of setting errors.
     *
     * @param settingErrors The new list of setting errors
     */
    public void setSettingErrors(List<String> settingErrors) {
        this.settingErrors = settingErrors;
    }
}
