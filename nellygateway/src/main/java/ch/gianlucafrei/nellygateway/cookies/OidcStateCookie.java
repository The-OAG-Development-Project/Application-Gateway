package ch.gianlucafrei.nellygateway.cookies;


public class OidcStateCookie {

    public static final String NAME = "oidc-state";

    private String provider;
    private String state;
    private String nonce;

    public OidcStateCookie() {}

    public OidcStateCookie(String provider, String state, String nonce) {
        this.provider = provider;
        this.state = state;
        this.nonce = nonce;
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
}
