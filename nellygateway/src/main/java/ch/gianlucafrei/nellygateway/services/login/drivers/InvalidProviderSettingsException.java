package ch.gianlucafrei.nellygateway.services.login.drivers;

import java.util.List;

public class InvalidProviderSettingsException extends RuntimeException {

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
