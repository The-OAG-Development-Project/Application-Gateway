package ch.gianlucafrei.nellygateway.config.configuration;

public class LoginProvider {

    private String type;
    private LoginProviderSettings with;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LoginProviderSettings getWith() {
        return with;
    }

    public void setWith(LoginProviderSettings with) {
        this.with = with;
    }
}
