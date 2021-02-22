package org.owasp.oag.cookies;

import org.owasp.oag.services.login.drivers.UserModel;

public class LoginCookie {

    public static final String NAME = "session";

    private int sessionExpSeconds;
    private String providerKey;
    private UserModel userModel;
    private String csrfToken;
    private String id;

    public LoginCookie() {
    }

    public LoginCookie(int sessionExpSeconds, String provider, UserModel userModel, String id) {
        this.sessionExpSeconds = sessionExpSeconds;
        this.providerKey = provider;
        this.userModel = userModel;
        this.id = id;
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

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
