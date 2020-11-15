package ch.gianlucafrei.nellygateway.cookies;


public class OidcStateCookie {

    public static final String NAME = "oidc-state";

    private String provider;
    private String state;
    private String nonce;
    private String returnUrl;

    public OidcStateCookie() {}

    public OidcStateCookie(String provider, String state, String nonce, String returnUrl) {
        this.provider = provider;
        this.state = state;
        this.nonce = nonce;
        this.returnUrl =returnUrl;
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

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
