package ch.gianlucafrei.nellygateway.services.oidc;

public class OIDCCallbackResult {

    public boolean success;

    public String subject;
    public String issuer;

    public String originalToken;
}
