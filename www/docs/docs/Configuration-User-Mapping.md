# User Mapping

The user mapping configuration defines how OAG tells the backend which the user is. There are currently three types of user mapping supported. The default one is the jwt-mapping which transports the user information in a signed jwt token. This has the advantage that the user information cannot be forged by anyone who does not have access to the key. With the `jwt-mapping`, the backend system must validate the token. The jwt-mapping recommended method.

If the backend does not validate the token, the `no-mapping can` be used, which does not add any user information to the request. This might be useful for a server that hosts only public static files like a SPA frontend and does not need any authentication.

If the backend does not support jwt based authentication, the `header-mapping` can be used, which transports the user information as plain HTTP headers. In that case, one must make sure that the backend can only be reached by the OAG because anyone could add the same header to the request who has access to the backend.

## JWT-Mapping

```yaml
securityProfiles:
  webapplication:
    userMapping:
      type: "jwt-mapping"
      settings:
        headerName: "Authorization"
        headerPrefix: "Bearer "
        audience: "<<route-url>>"
        issuer: "<<hostUri>>"
        tokenLifetimeSeconds: 30
        signatureImplementation: "rsa"
        signatureSettings: {}
        mappings:
          provider:       <session.provider>
          email_verified: <mappings.email_verified>
          email:          <mappings.email>
          proxy:          "OWASP Application Gateway"
```

### Settings

- `headerName`: Defines the header in which the jwt will be transported to the backend.
- `headerPrefix`: Prefix that is added before the jwt.
- `audience`: Value of the audience claim of the jwt. Can be any string or &lt;&lt;route-url&gt;&gt; which will use the url of the route as audience value.
- `issuer`: Issuer value for all strings. The special value &lt;&lt;hostUri&gt;&gt; will use the URI of OAG as defined in the main configuration.
- `tokenLifetimeSeconds`: Defines how long the jwt tokens are valid. The token are cached and used for multiple request to the backend.
- `signatureImplementation`: Defines which implementation for the jwt signature provider is used. Currently the supported values are: `rsa`, `hmac`. </br>
See [Key Management section Jwt signer](/docs/Key-management-and-JWT-signer).
- `signatureSettings`: Settings for the jwt signature provider.
- `mappings`: List of additional claims. Key is the name of the claim in the jwt token. </br>
See [Mapping Syntax](#mapping-syntax).

### Mappings

The following claims are always added to the jwt token:

- `sub`: Id of the user
- `aud`: Audience value as defined in the settings above
- `iat`, `nbf`: Time when the jwt token was issued by OAG
- `exp`: Expiry time of the jwt token. (iat + tokenLifetime)
- `iss`: Issuer value as defined in the settings above
- `jti`: Random 64bit hex value as token identifier
- `provider`: The name of the login provider that the user used as defined in the loginProviders settings.


The above configuration would create a JWT token that looks more or less like the following:

```
{
  "alg": "HS256"
}.{
  "sub": "0000000000000000000000",
  "aud": "https://backendurl.org",
  "proxy": "OWASP Application Gateway",
  "nbf": 1615794294,
  "email_verified": "true",
  "provider": "google",
  "iss": "http://localhost:8080",
  "exp": 1615794324,
  "iat": 1615794294,
  "jti": "d537dae82ef13f16",
  "email": "user@email.com"
}.[Signature]
```
Note: For algorithms such as RS256 OAG would provide additional headers kid (key id of the signing key) and jku (url where the public key matching kid can be loaded from)

### No-Mapping

If OAG should not attach any information about the user to the request the following setting can be used:

```yaml
securityProfiles:
  webapplication:    
    userMapping:
      type: "no-mapping"
      settings: {}
```

### Header-Mapping

If transporting the user information in a jwt token is not an option, the header-mapping can be used as an alternative.

::: warning
Please be careful when using the header-mapping because it's less secure than the jwt-mapping.
:::

When the header mapping is used, the following two additional security measures are needed:
* Make sure the backend can only be reached by OAG through network configuration (Anyone can craft headers that look like this)
* Add a shared secret (api key) to the request and verify it's presence on the backend.

```yaml
securityProfiles:
  webapplication:
    userMapping:
      type: "header-mapping"
      settings:
        mappings:
          X-USER-PROVIDER: <<login-provider>>
          X-USER-ID: <<user-id>>
          Authorization: env:APIKEY
```

### Mapping Syntax

You can define custom mappings with a string templating syntax. Under the hood jwt-mapping and header-mapping use [stringtemplate4](https://github.com/antlr/stringtemplate4). But please be aware that complex mapping (conditionals and so on) are not tested yet. The available objects are `session` and `mappings` where the session object hold information about the user session and mappings contains all mappings from the user model.

Examples:
- Simple strings are interpreted as constants: `OWASP Application Gateway`-> "OWASP Application Gateway"
- Name of the login provider: `<session.provider>` -> e.g. "Google"
- User Mapping: `<mappings.email>` -> user email
- Complex mapping: `provider=<session.provider>` -> e.g. "provider=Google""
You can also use if/else statements and functions in the mapping syntax if needed. </br>
See [stringtemplate4 cheatsheet](https://github.com/antlr/stringtemplate4/blob/master/doc/cheatsheet.md).

The session object contains the following values:

- `<session.sessionExpSeconds>`: Session expiry time (unix timestamp)
- `<session.remainingTimeSeconds>` Remaining session time in seconds
- `<session.provider>` Name of the used login provider
- `<session.id>` Id of the session
- `<session.userId>` Id of the user

The mappings object contains all mappings from the UserModel. Depends of the information for the login provider. i.e.: The oidc login provider class adds the following values to the user model if the are in the idToken from the authorization server:

- `<mappings.sub>`
- `<mappings.name>`
- `<mappings.given_name>`
- `<mappings.family_name>`
- `<mappings.middle_name>`
- `<mappings.nickname>`
- `<mappings.preferred_username>`
- `<mappings.profile>`
- `<mappings.picture>`
- `<mappings.website>`
- `<mappings.email>`
- `<mappings.email_verified>`
- `<mappings.gender>`
- `<mappings.birthdate>`
- `<mappings.zoneinfo>`
- `<mappings.locale>`
- `<mappings.phone_number>`
- `<mappings.phone_number_verified>`
- `<mappings.address>`
- `<mappings.updated_at>`

