package ch.gianlucafrei.nellygateway.cookies;


import javax.servlet.http.Cookie;

public class OidcStateCookie{

    public static final String NAME = "oidc-state";

    private String provider;
    private String sate;
    private String nonce;

    public OidcStateCookie(){

    }

    public OidcStateCookie(String provider, String sate, String nonce) {
        this.provider = provider;
        this.sate = sate;
        this.nonce = nonce;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSate() {
        return sate;
    }

    public void setSate(String sate) {
        this.sate = sate;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
