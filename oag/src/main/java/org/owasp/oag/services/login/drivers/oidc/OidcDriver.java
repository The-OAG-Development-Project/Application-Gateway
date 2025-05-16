package org.owasp.oag.services.login.drivers.oidc;

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
import org.owasp.oag.config.configuration.LoginProviderSettings;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.AuthenticationException;
import org.owasp.oag.exception.SystemException;
import org.owasp.oag.services.login.drivers.oauth.Oauth2Driver;
import org.owasp.oag.session.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * OIDC driver for OpenID Connect authentication.
 * <p>
 * This driver is responsible for handling the OIDC authentication flow, including loading tokens and user info.
 * It extends the Oauth2Driver class to provide additional functionality specific to OIDC.
 */
public class OidcDriver extends Oauth2Driver {

    private static final Logger log = LoggerFactory.getLogger(OidcDriver.class);

    /**
     * Constructor for OidcDriver.
     *
     * @param settings The login provider settings.
     */
    public OidcDriver(LoginProviderSettings settings) {
        super(settings);
    }

    /**
     * Returns the errors for the OIDC login provider settings.
     *
     * @param settings The login provider settings.
     * @return A list of scopes.
     */
    @Override
    public List<String> getSettingsErrors(LoginProviderSettings settings) {

        var errors = super.getSettingsErrors(settings);

        if (errors.isEmpty()) {
            if (!getScopes(settings).contains("openid"))
                errors.add("Scopes does not contain 'openid'");
        }

        return errors;
    }

    /**
     * loads the tokens from the token endpoint for the given cientAuth
     *
     * @param clientAuth the auth-info used to access the tokenEndpoint
     * @param tokenEndpoint the endpoint providing the tokens
     * @param codeGrant the AUthorizationGrant
     * @return The tokens
     * @throws AuthenticationException if the tokenEndpoint did not accept the auth-info
     */
    @Override
    protected Tokens loadTokens(ClientAuthentication clientAuth, URI tokenEndpoint, AuthorizationGrant codeGrant) throws AuthenticationException {

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, null);
        TokenResponse tokenResponse;
        HTTPResponse httpResponse;

        try {
            httpResponse = request.toHTTPRequest().send();
            tokenResponse = OIDCTokenResponseParser.parse(httpResponse);
        } catch (IOException | ParseException ex) {
            log.warn("Load token failed: {}", ex.getMessage());
            throw new SystemException("Could not load tokens", ex);
        }

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();

            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                log.warn("404 response from token endpoint");
            }

            String message = errorResponse.getErrorObject().getDescription();
            throw new AuthenticationException(message);
        }

        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        OIDCTokens oidcTokens = successResponse.getOIDCTokens();
        return oidcTokens;
    }

    /**
     * Loads the user info from the OIDC tokens.
     *
     * @param tokens The OIDC tokens.
     * @return A UserModel object containing the user info.
     */
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

            model.set("original-id-token", idToken.getParsedString());
            model.set("original-access-token", accessToken.toString());

            for (String claimName : getMappedClaims()) {

                Object claim = jwtClaims.getClaim(claimName);

                if (claim != null) {
                    model.set(claimName, claim.toString());
                }
            }

            return model;

        } catch (Exception e) {
            throw new ApplicationException("Could not extract user info", e);
        }
    }

    /**
     * Returns a list of claims that are mapped from the OIDC tokens to the user model.
     *
     * @return A list of claim names.
     */
    protected List<String> getMappedClaims() {

        return Arrays.asList("sub",
                "name",
                "given_name",
                "family_name",
                "middle_name",
                "nickname",
                "preferred_username",
                "profile",
                "picture",
                "website",
                "email",
                "email_verified",
                "gender",
                "birthdate",
                "zoneinfo",
                "locale",
                "phone_number",
                "phone_number_verified",
                "address",
                "updated_at");
    }
}
