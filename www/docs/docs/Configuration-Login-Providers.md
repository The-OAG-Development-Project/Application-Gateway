# Login Providers

OWASP Application Gateway uses the concept of login providers for all kinds of authentication. With the login providers' settings, you can specify one or multiple ways users can authenticate themselves.

Example:

```yaml
hosturi: https://example.com
loginProviders:
  youriam:
    type: oidc
    with:
      authEndpoint: https://youriam.com/auth
      tokenEndpoint: https://youriam.com/token
      clientId: foo
      clientSecret: env:IAM_CLIENT_SECRET
      scopes: [ "openid", "email" ]
      federatedLogoutUrl: https://youriam.com/logout?returnTo=https%3A%2F%2Fexample.com

```

## Configuration

### `type`

The `type` setting specifies which login provider implementation will be used. The `with` settings then sets the specific configuration for this login provider.

Currently, the following two type providers are supported:
- `oidc` can be used for any public or private identity provider that implements oidc
- `github` implements GitHub Oauth2 based authentication

#### `with`

The `with` property specifies additional settings for the login provider. Please find the detailed configuration bellow:

## Login Provider Types

### OpenId-Connect

OWAG implements OpenId-Connect based authentication with the auth-code flow.

- `authEndpoint`: Specifies where URI of the authorization endpoint. The user will be redirected to this url
- `tokenEndpoint`: Specifies the token endpoint from where OWAG can load the access and id token after the user was redirected back
- `clientId`: Specifies the OAuth2 client id that is used by OWAG
- `clientSecret`: Specifies the Oauth2 client secret that is sent by OWAG when the token endpoint is called. Danger: This is usually a sensitive value. It is therefore recommended to inject it via a PATH variable during runtime with the env: `prefix`.
- `scopes`: Specifies the list of scopes that OWAG requests from the identity provider. Technically only `openid` is required, but you can also add additional scopes like `email` or idp specific scopes.
- `federatedLogoutUrl`(optional) OIDC does not define how user can be logged out at the authorization server. However, most IPS provide a logout endpoint that can be used to log the user out, also on a IDP level. For first-party If this url is set the user will be redirected to it after the logout on OAG. If set, this overwrites the `redirectLogout` from the session behaviour configuration.

See also: [OAuth Authorization Code Grant Type](https://developer.okta.com/blog/2018/04/10/oauth-authorization-code-grant-type)

### GitHub 

Because GitHub authentication is also based on Oauth2 (Like OpenID-Connect is based on Oauth2) the configuration is very similar with the only difference that you need to request at least `user` and `email` as scopes. If you want to use the GitHub API on behalt of the user with your application you can specify additional scopes. You can find more information about GitHub specific scopes [here](https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/).

## Sign-in endpoint

The name of the login provider specifies the Sign-in endpoint your application needs to call to login the user. The Sign-in endpoint is always `/auth/{name of the login provider}/login`

For the example before your application need redirect the user to `https://example.com/auth/youriam/login`


## Sample configuration for different identity providers

### Google
```yaml
    google:
        type: oidc
        with:
          authEndpoint: https://accounts.google.com/o/oauth2/auth
          tokenEndpoint: https://oauth2.googleapis.com/token
          clientId: <google client id>
          clientSecret: <google client secret>
          scopes: [ "openid", "email" ]
```
### Auth0

```yaml
  auth0:
    type: oidc
    with:
        authEndpoint: https://<yourdomain>.auth0.com/authorize
        tokenEndpoint: https://<yourdomain>.auth0.com/oauth/token
        clientId: < auth0 client id>
        clientSecret:  < auth0 client secret>
        scopes: [ "openid", "email" ]
        federatedLogoutUrl: https://<yourdomain>.auth0.com/v2/logout?client_id= < auth0 client id>&returnTo=http%3A%2F%2Flocalhost%3A8080
```
### GitHub

Github does not implement OIDC. This is why we need to use the special github driver.

See also: [Creating an OAuth app](https://developer.github.com/apps/building-oauth-apps/creating-an-oauth-app/)

```yaml
github:
    type: github
    with:
      authEndpoint: https://github.com/login/oauth/authorize
      tokenEndpoint: https://github.com/login/oauth/access_token
      clientId: <your github client id>
      clientSecret: env:GITHUB_CLIENT_SECRET
      scopes: [ "user", "email" ]
```