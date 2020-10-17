package ch.gianlucafrei.nellygateway.services.oidc;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

public class OIDCLoadTokenResult {

    public boolean success;
    public String errorMessage;

    public JWT idToken;
    public AccessToken accessToken;
    public RefreshToken refreshToken;
}
