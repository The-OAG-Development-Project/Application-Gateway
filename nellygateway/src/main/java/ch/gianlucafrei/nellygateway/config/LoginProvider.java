package ch.gianlucafrei.nellygateway.config;

public class LoginProvider {

    private String type;
    private LoginProviderSettings with;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public LoginProviderSettings getWith() {
        return with;
    }

    public void setWith(LoginProviderSettings with) {
        this.with = with;
    }
}
