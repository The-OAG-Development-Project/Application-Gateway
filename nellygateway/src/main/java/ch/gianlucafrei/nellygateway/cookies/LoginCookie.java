package ch.gianlucafrei.nellygateway.cookies;

import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;

public class LoginCookie {

    public static final String NAME = "session";
    public static final String SAMESITE = "lax";

    private int sessionExpSeconds;
    private String providerKey;
    private UserModel userModel;

    public LoginCookie() {
    }

    public LoginCookie(int sessionExpSeconds, String provider, UserModel userModel) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.providerKey = provider;
        this.userModel = userModel;
    }

    public long getSessionExpSeconds() {
        return sessionExpSeconds;
    }

    public void setSessionExpSeconds(int sessionExpSeconds) {
        this.sessionExpSeconds = sessionExpSeconds;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
