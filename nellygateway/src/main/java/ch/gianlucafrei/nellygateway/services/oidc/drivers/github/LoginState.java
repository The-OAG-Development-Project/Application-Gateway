package ch.gianlucafrei.nellygateway.services.oidc.drivers.github;

import java.io.Serializable;
import java.net.URI;

public class LoginState {

    private URI redirectURI;
    private Serializable state;
}
