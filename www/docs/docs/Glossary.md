# Glossary

This glossary defines the terms used throughout the OWASP Application Gateway (OAG)
documentation and configuration. Where a term maps directly to a configuration key or a
value used in the configuration file, it is shown in `code font`.

## Most relevant terms

These are the most important terms to understand first, and what they mean in the context of OAG.

| Term | Explanation |
| ---- | ----------- |
| Downstream System | Any system or application that is called (forwarded to) by OAG. Often services and applications implementing business logic. |
| Upstream System | Callers (initiating requests) of OAG. Often also known as clients. |
| Logging | Writing information about the inner workings (state) of OAG to a file or other destination. |
| Tracing | The goal of tracing is to follow a flow of activity and data processing (often known as request) through one or more systems/applications involved. |
| Log correlation / Correlation logging | The ability to assign log statements to a flow, effectively enabling tracing. Usually log correlation is implemented by assigning a unique id to each flow and writing this unique id out with every log statement. |

## All terms

The following table lists all terms alphabetically.

| Term | Definition |
| ---- | ---------- |
| Access token | The OAuth2 token OAG receives from the token endpoint after a successful login. OAG uses the login result to build the user's session; it is not forwarded to downstream systems as-is. |
| `allowAnonymous` | Per-route boolean. When `no`, OAG only forwards authenticated requests to the route; when `yes`, unauthenticated requests are allowed through. |
| `allowedMethods` | Per-security-profile list of permitted HTTP methods. Requests using a method not in the list are rejected with `405 Method Not Allowed`. Also called method whitelisting. |
| API key | A shared secret sent by OAG to a downstream system (for example via the `requestHeader` user mapping) so the backend can verify that a request originated from OAG. |
| `apifornonebrowsers` | Predefined security profile for APIs that are not called from a browser. Removes most browser-oriented security features of OAG. |
| `apiforspa` | Predefined security profile for an API called from a browser (single-page application). Adds CSRF protection using the double-submit-cookie pattern. |
| Authorization Code Flow | The OAuth2 / OpenID Connect flow OAG uses for login: the user is redirected to the authorization endpoint, returns with a code, and OAG exchanges the code for tokens at the token endpoint. Also called auth-code flow. |
| Authorization endpoint (`authEndpoint`) | The identity provider URL to which OAG redirects the user to begin authentication. |
| Authorization server | See Identity Provider (IdP). |
| Backend | See Downstream system. |
| Bean name | The Spring component name of a pluggable implementation. Configuration `type` values (trace, CSRF, key rotation, etc.) resolve to a bean by name, which is how custom code extensions are selected. |
| Claim | A key/value entry inside a JWT. OAG always sets `sub`, `aud`, `iat`, `nbf`, `exp`, `iss`, `jti`, and `provider`, and adds custom claims defined in the `mappings` section of the user mapping. |
| Client | See Upstream system. |
| `clientId` / `clientSecret` | The OAuth2 client credentials OAG presents to the identity provider. The secret is sensitive and is typically injected at runtime with the `env:` prefix. |
| Content Security Policy (CSP) | A response header (`Content-Security-Policy`) OAG can set through a security profile to restrict the sources a browser may load content from. |
| Cookie encryption (JWE) | OAG stores session data in cookies encrypted as JSON Web Encryption (JWE) using AES-256-GCM, so the client cannot read or tamper with the session. |
| Correlation ID | See Trace ID. |
| CSRF (Cross-Site Request Forgery) | An attack in which a victim's browser is tricked into sending an authenticated request. OAG mitigates it per security profile via the `csrfProtection` setting. |
| `csrfProtection` | Per-security-profile setting selecting the CSRF strategy: `double-submit-cookie`, `samesite-strict-cookie`, `none`, or a custom implementation. |
| `csrfSafeMethods` | Per-security-profile list of HTTP methods excluded from CSRF protection (typically `GET`, `HEAD`, `OPTIONS`). |
| Custom (code) extension | A pluggable Java component (e.g. implementing `CsrfProtectionValidation`, a trace context, a JWT signer, or a key store) that OAG selects by its bean name from configuration. |
| `defaultKeyRotation` | The built-in key rotation strategy. Generates a fresh signing key at startup, rotates it on the configured interval, and publishes keys to the JWK store. |
| Design principles | OAG's three guiding principles: secure by default (hardening out of the box), stateless (session state in encrypted client cookies), and configuration-based (behavior driven by one central config file). |
| `double-submit-cookie` | CSRF strategy where OAG requires the value of the `csrf` cookie to be echoed in the `X-CSRF-TOKEN` header (or a `CSRFToken` form field). OAG cryptographically binds the CSRF cookie to the session cookie. |
| Downstream system | Any service or application OAG forwards requests to — typically a backend implementing business logic. Also called downstream service or backend. |
| `env:` prefix | Configuration value prefix that instructs OAG to read the value from an environment variable at runtime instead of storing it in the config file (used for secrets such as `clientSecret`). |
| Federated logout (`federatedLogoutUrl`) | Optional login-provider URL to which OAG redirects the user after logout so the session is also ended at the identity provider. When set, it overrides `redirectLogout`. |
| `forwardIncomingTrace` | Trace profile setting. When `false` (recommended at a trust boundary), OAG generates a new trace ID for every incoming request instead of reusing one supplied by an upstream system. |
| Gateway | See OAG. |
| GitHub provider (`github`) | Login provider type implementing GitHub's OAuth2 login. Requires at least the `user` and `email` scopes. GitHub does not implement OIDC, hence a dedicated driver. |
| `hmac` | JWT signer implementation producing HS256/HS384/HS512 signatures (chosen by key length) using a shared secret. Not recommended; adds a `kid` header but no `jku`. |
| `hostUri` | Top-level configuration value defining the public base URI under which OAG is reachable. Used, for example, as the default JWT `issuer`. |
| HSTS (`Strict-Transport-Security`) | HTTP Strict Transport Security response header OAG can set through a security profile to force browsers to use HTTPS. |
| HTTPS redirection | Built-in OAG feature that redirects HTTP requests to HTTPS while honoring proxy headers (proxy awareness). |
| ID token | The OpenID Connect token issued by the identity provider describing the authenticated user. OAG reads its claims into the user model for user mapping. |
| Identity Provider (IdP) | The external service that authenticates users and issues tokens (e.g. Google, Auth0, an internal OIDC server). Also called authorization server. |
| Issuer (`iss`) | JWT claim identifying who issued the token. For OAG-signed tokens it defaults to `<<hostUri>>`. |
| `jku` | JWT header carrying the URL of the JWK Set where the matching public key can be fetched. Added by the `rsa` JWT signer. |
| JWK (JSON Web Key) | A JSON representation of a cryptographic key. OAG publishes its RSA signing public keys as JWKs. |
| JWK store (`jwkStoreProfile`) | Component holding OAG's signing keys. The built-in `localRsaJwkStore` keeps valid keys (plus a grace period) in memory only. |
| JWKS / JWK Set | A set of JWKs. OAG exposes its current and historical signing public keys as a JWK Set (RFC 7517) at `/.well-known/jwks` (and `/.well-known/jwks/<kid>`). |
| JWS (JSON Web Signature) | The signature standard (RFC 7515) a downstream system verifies to confirm an OAG-issued JWT is authentic and unmodified. |
| JWT (JSON Web Token) | A signed token OAG can issue to convey user identity to downstream systems (the `jwtToken` user mapping). The backend validates the signature using OAG's published public key. |
| JWT signer (`signatureImplementation`) | The component that signs user-mapping JWTs. Supported values: `rsa` (RS256, recommended) and `hmac` (HS256/384/512, not recommended). |
| Key generator (`keyGeneratorProfile`) | Component that creates signing keys. The built-in `rsaKeyGenerator` produces RSA keys of the configured `keySize`. |
| Key management profile (`keyManagementProfile`) | Top-level configuration grouping the key rotation, JWK store, and key generator profiles that together manage OAG's signing keys. |
| Key rotation (`keyRotationProfile`) | Configuration controlling how often OAG generates a new signing key (`signingKeyRotationSeconds`) and cleans up expired keys (`cleanupFrequencySeconds`). |
| `kid` (key ID) | Identifier of a specific signing key. Present in the JWT header and usable to fetch that key from the JWKS endpoint. |
| `localRsaJwkStore` | Built-in JWK store implementation that keeps RSA signing keys in memory locally for as long as they are valid (plus a grace period). |
| Log correlation / Correlation logging | Assigning log statements to a single flow by writing a shared trace ID with every log statement, which makes tracing across systems possible. |
| Logging | Writing information about OAG's internal state and activity to a file or other destination. |
| Login driver | The code component implementing a login provider. It builds the authorization redirect (returning the auth URI and `state`) and turns the provider's callback into a user model. |
| Login provider | A configured way for users to authenticate (`loginProviders` section). Each provider has a `type` (`oidc` or `github`) and provider-specific settings under `with`. |
| Method whitelisting | See `allowedMethods`. |
| `no` (user mapping) | User mapping type that adds no user information to the forwarded request. Suitable for backends serving only public content. |
| `noTraceContext` | Trace implementation that disables tracing toward downstream and upstream systems, using a trace ID only for OAG's own logs. Not recommended. |
| OAG (OWASP Application Gateway) | An HTTP reverse proxy that sits between clients and web applications and handles OAuth2/OIDC login, session management, and the security tokens forwarded to backends. |
| OAuth2 | The authorization framework OAG builds on for login. Both the `oidc` and `github` providers use OAuth2. |
| OIDC provider (`oidc`) | Login provider type for any identity provider implementing OpenID Connect. OAG uses the authorization code flow. |
| OpenID Connect (OIDC) | An identity layer on top of OAuth2 that OAG uses to authenticate users and obtain an ID token. |
| Path (`path`) | Per-route ant-style pattern deciding which incoming request paths the route handles (`/svc/`, `/svc/*`, `/svc/**`). |
| Persistent map | OAG's key/value storage abstraction (`PersistentMap`) with `FilePersistentMap` and `InMemoryPersistentMap` implementations. Backs, for example, the session blacklist. |
| Predefined profile | A security profile shipped with OAG: `static`, `webapplication`, `apiforspa`, `apifornonebrowsers`. Any can be overridden or supplemented in the config file. |
| Provider | See Login provider. Also the JWT/session `provider` value naming which login provider the user authenticated with. |
| `redirectLoginFailure` / `redirectLoginSuccess` / `redirectLogout` | Session-behaviour URLs OAG redirects the user to after a failed login, a successful login, and logout respectively. |
| `renewWhenLessThan` | Session-behaviour setting enabling a rolling session: if a still-valid session has less than this many seconds left, OAG issues a fresh session of full `sessionDuration`. |
| `requestHeader` (user mapping) | User mapping type that conveys user identity as plain HTTP headers. Less secure than `jwtToken`; requires network isolation and typically a shared API key. |
| Response headers (`responseHeaders`) | Per-security-profile rules that add, overwrite (`value`), or remove (`<<remove>>`) headers on responses returned to the client. |
| Reverse proxy | A server that receives client requests and forwards them to backend servers, returning the backend's response. OAG is a reverse proxy. |
| Rewrite (`rewrite`) | Per-route rule controlling how the request path is transformed before being forwarded to the downstream URL. Defaults to stripping the matched path prefix; can be customized with `regex`/`replacement`. |
| Rolling session | A session that is automatically extended while the user stays active, controlled by `renewWhenLessThan`. |
| Route | A configuration entry (under `routes`) mapping an incoming `path` to a downstream `url`, using a named security profile (`type`) and an `allowAnonymous` policy. |
| `rsa` | Recommended JWT signer implementation. Produces RS256 signatures using automatically rotated keys and adds `kid` and `jku` headers. |
| `rsaKeyGenerator` | Built-in key generator that produces RSA keys of the configured `keySize`. |
| `samesite-strict-cookie` | CSRF strategy relying on a SameSite=Strict session cookie so the browser does not send it on cross-site requests. |
| Scopes (`scopes`) | The OAuth2/OIDC scopes OAG requests from the identity provider. `openid` is required for OIDC; others (e.g. `email`) are optional. |
| Security headers | The set of hardening HTTP response headers OAG sets by default through security profiles, such as `X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy`, `Referrer-Policy`, and `Permissions-Policy`. |
| Security profile | A named bundle of security settings — `allowedMethods`, `csrfProtection`, `responseHeaders`, `userMapping` — referenced by a route's `type` and applied to all its requests. |
| Security token | A token used to convey authenticated identity or authorization, such as an OAuth2 access token, an OIDC ID token, or an OAG-issued JWT. |
| Session | The authenticated state of a user with OAG. OAG is stateless: session data is stored in an encrypted cookie on the client. |
| Session blacklist | A store of invalidated session identifiers (for example after logout). An identifier stays blacklisted for at least its TTL so the session cannot be reused. |
| Session cookie | The secure, HTTP-only, SameSite cookie in which OAG stores encrypted session information on the client. |
| `sessionBehaviour` | Configuration section defining session handling: `sessionDuration`, `renewWhenLessThan`, and the post-login/logout redirect URLs. |
| `sessionDuration` | Number of seconds a session cookie is valid after authentication (default 3600). |
| Sign-in endpoint | The OAG URL a client calls to start login for a provider: `/auth/{login-provider-name}/login`. |
| `simpleTraceContext` | Trace implementation using a random UUID as the trace ID and, by default, the `X-Correlation-Id` header to propagate it. |
| Single-page application (SPA) | A browser front-end served as static files that calls APIs. The `static` and `apiforspa` profiles target this case. |
| `state` | The OAuth2 state parameter OAG generates during login and validates on the callback to maintain state and protect the login flow. |
| `static` (profile) | Predefined security profile for static content: default security headers and only `GET`, `HEAD`, `OPTIONS` allowed. |
| Stateless | A design principle of OAG: no server-side session storage, so it can run on multiple nodes without shared state. All session state lives in encrypted client cookies. |
| TLS configuration | Configuration of the certificate and TLS settings OAG serves over HTTPS. OAG ships a self-signed localhost certificate that is meant to be replaced for production. |
| Token endpoint (`tokenEndpoint`) | The identity provider URL from which OAG retrieves the access and ID tokens after the user is redirected back with an authorization code. |
| Trace ID | A unique identifier applied to each request reaching OAG and propagated to downstream (and optionally upstream) systems to enable tracing. Also called correlation ID. Available in logs as `oag.CorrId`. |
| Trace profile (`traceProfile`) | Configuration section controlling tracing behavior: implementation `type`, whether to forward/return trace info, and length limits. |
| Tracing | Following a single flow of activity and data as it passes through one or more systems, enabled by propagating a shared trace ID. |
| Trust boundary / trust zone | The edge of a trusted network area. OAG typically sits at the trust boundary, which is why it generates its own trace IDs rather than trusting upstream-supplied ones. |
| `trustedRedirectHosts` | Configured allowlist of hosts OAG is permitted to redirect to, guarding the login/logout `returnUrl` against open-redirect attacks. |
| `type` | Context-dependent selector: on a route it names the security profile; on a login provider it names the provider driver (`oidc`/`github`); in the key/trace profiles it names the implementation to use. |
| Upstream system | A caller that initiates requests to OAG — often a browser or client application. |
| URL (`url`) | Per-route downstream base URL to which matching requests are forwarded (after path rewriting). |
| User mapping (`userMapping`) | The security-profile section defining how OAG communicates the authenticated user to the backend. Types: `jwtToken` (recommended), `requestHeader`, `no`. |
| User model | The internal representation of the authenticated user, populated from the identity provider's ID token claims and referenced in mapping expressions as `mappings.*`. |
| `w3cTraceContext` | Default trace implementation, compliant with the W3C Trace Context specification, using its standard headers to propagate the trace ID. |
| W3C Trace Context | A W3C specification for propagating trace context via standard HTTP headers, implemented by OAG's default `w3cTraceContext`. |
| `webapplication` (profile) | Predefined security profile suitable for most web applications: allows the common HTTP methods and adds SameSite-cookie CSRF protection. |
| Whitelisting (URLs) | The set of OAG URLs that must be reachable through a firewall or WAF, including the `/auth**` authentication endpoints, the configured routes, and the `/.well-known/jwks**` and `/oag/**` operational endpoints. |
