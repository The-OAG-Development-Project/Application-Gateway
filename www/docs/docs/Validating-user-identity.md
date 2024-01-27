# Validating user identity

OAG forwards the identity of the user according to its configuration in one of the following ways: </br>

:::tip
See [Configure User mapping](/docs/Configuration-User-Mapping) for details on how to configure these options.
:::

## Signed JWT issued by OAG (recommended)
Most important is proper signature validation. For this you need the public key matching OAG's signing key. It is up to you how you make this key available to your application (e.g. within a keystore, using JWKS of OAG, providing BASE64 encoded in a config file, etc.). The recommended way is to use OAGs [JWKS endpoint](/docs/JWKS-JWT-signing-public-keys-of-OAG). If you take that route consider the following as the minimal verification your application should do after receiving the OAG Token (JWT):
1. From the configured header sent by OAG extract the ID Token and read of the ID Token:
    * kid -> the id of the key used to sign the Token
    * jku -> The url where the JWKS endpoint of OAG is published
2. verify that jku points to a trusted / known domain (i.e. https://&lt;jourknownhost.domain&gt;:&lt;port&gt;/)
3. load the keys from jku and extract the key matching kid -> this is the signing public key, SPK. 

:::tip
Make sure you enable proper TLS hostname verification and use a truststore with minimal trusted CA certificates when establishing the https connection. Alternativile you can also use jku/kid to request the JWK with kid directly.
:::

4. Verify the signature of the Token with SPK according to [RFC 7515](https://tools.ietf.org/html/rfc7515)
5. Verify the issuer (OAG, as configured)
6. Verify the audience (aud claim should be as configured and expected from your application)
7. verify the JWT according to your needs (See also: [OIDC](https://openid.net/specs/openid-connect-core-1_0.html) and [RFC7519](https://datatracker.ietf.org/doc/html/rfc7519)). At least this should include:
    * exp -> not expired
    * jti -> when you do not expect repeated calls with the same token, or to check for tokens marked as "invalid"
    * sub -> The user is granted access to the requested resource/action

## Forwarded JWT of the original authentication server (OIDC server) (recommended only when OIDC server is owned by your company/project and the only one)
Verify the forwarded JWT/Access Token as specified by the OIDC server / authentication server.

## Header fields with user data (not recommended, for legacy integration only)
Verify the headers according to your needs and custom security concept.