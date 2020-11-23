package ch.gianlucafrei.nellygateway.cookies;

public class LoginStateCookie {

    public static final String NAME = "state";

    private String provider;
    private String state;
    private String returnUrl;

    public LoginStateCookie() {
    }

    public LoginStateCookie(String provider, String state, String returnUrl) {
        this.provider = provider;
        this.state = state;
        this.returnUrl = returnUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
