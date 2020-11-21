package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import java.io.Serializable;
import java.net.URI;

public class LoginState {

    private URI redirectURI;
    private Serializable state;

    public LoginState(URI redirectURI, Serializable state) {
        this.redirectURI = redirectURI;
        this.state = state;
    }

    public URI getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(URI redirectURI) {
        this.redirectURI = redirectURI;
    }

    public Serializable getState() {
        return state;
    }

    public void setState(Serializable state) {
        this.state = state;
    }
}
