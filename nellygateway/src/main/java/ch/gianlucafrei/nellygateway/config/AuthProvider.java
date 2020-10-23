package ch.gianlucafrei.nellygateway.config;

public class AuthProvider {

    private String authEndpoint;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private int sessionDuration;

    private String redirectSuccess;

    public String getTokenEndpoint() {
        return tokenEndpoint;
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

}
