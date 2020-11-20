package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class LoginDriverBase implements LoginDriver{

    private String providerKey;

    public LoginDriverBase(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getProviderKey() {
        return providerKey;
    }

    protected URI getCallbackUri(){

        String callback = String.format("%s/auth/%s/callback", NellygatewayApplication.config.hostUri, providerKey);

        try {
            return new URI(callback);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot compute callback URI for provider " + providerKey, e);
        }
    }
}
