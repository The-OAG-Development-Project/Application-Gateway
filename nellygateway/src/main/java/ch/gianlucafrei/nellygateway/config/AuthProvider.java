package ch.gianlucafrei.nellygateway.config;

import java.net.URI;
import java.net.URISyntaxException;

public class AuthProvider {

    private String authEndpoint;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private int sessionDuration;
    private String[] scopes = new String[] {"openid"};
    private String redirectSuccess;
    private String driver;

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public URI getTokenEndpointAsURI(){
        try {
            return new URI(tokenEndpoint);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid token endpoint");
        }
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public URI getAuthEndpointAsURI() {
        try {
            return new URI(authEndpoint);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid auth endpoint");
        }
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public String getRedirectSuccess() {
        return redirectSuccess;
    }

    public void setRedirectSuccess(String redirectSuccess) {
        this.redirectSuccess = redirectSuccess;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
