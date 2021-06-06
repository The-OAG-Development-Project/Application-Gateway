package org.owasp.oag.services.login.drivers;

import org.owasp.oag.exception.ConfigurationException;

import java.util.List;

public class InvalidProviderSettingsException extends ConfigurationException {

    private List<String> settingErrors;

    public InvalidProviderSettingsException(List<String> settingErrors) {

        super("Invalid provider settings: " + formatErros(settingErrors));
        this.settingErrors = settingErrors;
    }

    public static String formatErros(List<String> errors) {
        return String.join(", ", errors);
    }

    public List<String> getSettingErrors() {
        return settingErrors;
    }

    public void setSettingErrors(List<String> settingErrors) {
        this.settingErrors = settingErrors;
    }
}
