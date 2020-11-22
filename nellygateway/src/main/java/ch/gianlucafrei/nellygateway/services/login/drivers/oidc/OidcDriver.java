package ch.gianlucafrei.nellygateway.services.login.drivers.oidc;

import ch.gianlucafrei.nellygateway.config.LoginProviderSettings;
import ch.gianlucafrei.nellygateway.services.login.drivers.AuthenticationException;
import ch.gianlucafrei.nellygateway.services.login.drivers.UserModel;
import ch.gianlucafrei.nellygateway.services.login.drivers.oauth.Oauth2Driver;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class OidcDriver extends Oauth2Driver {

    public OidcDriver(LoginProviderSettings settings, URI callbackURI) {
        super(settings, callbackURI);
    }

    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors =  super.getSettingsErrors(settings);

        if(errors.isEmpty())
        {
            if(! getScopes(settings).contains("openid"))
                errors.add("Scopes does not contain 'openid'");
        }

        return errors;
    }

    @Override
    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        TokenResponse tokenResponse;

        try{
            HTTPResponse httpResponse = request.toHTTPRequest().send();
            tokenResponse = OIDCTokenResponseParser.parse(httpResponse);
        }
        catch (IOException | ParseException ex)
        {
            throw new RuntimeException("Could not load tokens", ex);
        }

        if (! tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            String message = errorResponse.getErrorObject().getDescription();
            throw new AuthenticationException(message);
        }

        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        OIDCTokens oidcTokens = successResponse.getOIDCTokens();
        return oidcTokens;
    }

    @Override
    protected UserModel loadUserInfo(Tokens tokens) {

        try {

            // Because we have overridden the loadTokens method we can safely convert the tokens object
            OIDCTokens oidcTokens = (OIDCTokens) tokens;
            JWT idToken = oidcTokens.getIDToken();
            JWTClaimsSet jwtClaims = idToken.getJWTClaimsSet();

            AccessToken accessToken = oidcTokens.getAccessToken();
            RefreshToken refreshToken = oidcTokens.getRefreshToken();

            UserModel model = new UserModel(jwtClaims.getSubject());
            model.set("id-token", idToken.getParsedString());
            model.set("access-token", accessToken.toString());
            model.set("refreshToken", refreshToken != null ? refreshToken.toString() : null);

            // TODO create logic for claim mappings
            Object email = jwtClaims.getClaim("email");
            if(email != null)
                model.set("email", email.toString());

            Object phone = jwtClaims.getClaim("phone");
            if(phone != null)
                model.set("phone", phone.toString());

            return model;

        } catch (Exception e) {
            throw new RuntimeException("Could not extract user info", e);
        }
    }
}
