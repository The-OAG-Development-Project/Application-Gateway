package org.owasp.oag.controllers.dto;

public class SessionInformation {

    public static final String SESSION_STATE_AUTHENTICATED = "authenticated";
    public static final String SESSION_STATE_ANONYMOUS = "anonymous";

    private String state;
    private int expiresIn;


    public SessionInformation(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
