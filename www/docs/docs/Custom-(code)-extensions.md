# Overview

OAG is built in an extendable way. This means that for many aspects you can develop you own implementation of certain functionality and "hook" them into OAG.
This section introduces you to how you can create and hook in your code for the different aspects.

## How an extension is selected

An extension is a Spring bean. OAG resolves most extension points by **bean name** from a `type` value in the
configuration file, so annotating your implementation with `@Component("myImplementation")` and setting the
matching `type` is all the wiring that is needed. Some extension points are resolved by naming convention
(CSRF) or by collecting every bean of the type (session hooks); this is noted per interface below.

Extensions run on the request's virtual thread and use the servlet API (`jakarta.servlet.http`). They are
plain sequential Java — blocking calls are fine and no reactive types appear in any signature.

## Extension points

### `org.owasp.oag.services.csrf.CsrfProtectionValidation`

Decides whether a request is blocked as a suspected CSRF attack.

```java
boolean needsRequestBody();
boolean shouldBlockRequest(HttpServletRequest request, String requestBody);
```

Called by the CSRF route filters for every proxied request whose method is not listed in the security
profile's `csrfSafeMethods`. `needsRequestBody()` selects which of the two filters applies: when it returns
`false` the request is validated before the body is read and `requestBody` is `null`; when it returns `true`
the body is buffered first and passed in.

Resolved by naming convention: the `csrfProtection` value `foo` loads the bean whose class is named
`CsrfFooValidation` (see `org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory`).

### `org.owasp.oag.services.tokenMapping.UserMapper`

Adds the authenticated user's identity to the request that is forwarded downstream.

```java
ServerRequest mapUserToRequest(ServerRequest request, GatewayRouteContext context);
```

`ServerRequest` is `org.springframework.web.servlet.function.ServerRequest` and
`GatewayRouteContext` is `org.owasp.oag.filters.GatewayRouteContext`, which carries the route name, the
route and security-profile configuration, the request and upstream URI, and the optional
`org.owasp.oag.session.Session`.

Called by the user-mapping route filter once per proxied request, after access control and CSRF validation.
`ServerRequest` is immutable, so an implementation returns a new request built with
`ServerRequest.from(request)`; returning the argument unchanged adds no user information.

Selected per security profile through the `userMapping` `type` value.

### `org.owasp.oag.hooks.session.SessionHook`

Participates in the session lifecycle, typically to set or clear cookies.

```java
void createSession(Map<String, Object> filterContext, HttpServletResponse response);
void renewSession(Map<String, Object> filterContext, HttpServletResponse response);
void destroySession(Map<String, Object> filterContext, HttpServletRequest request, HttpServletResponse response);
int order();
```

Called by `org.owasp.oag.hooks.session.SessionHookChain` when a user logs in (`createSession`), when a
rolling session is extended because it has less than `renewWhenLessThan` seconds left (`renewSession`), and
when a user logs out (`destroySession`). The `filterContext` map is shared between all hooks of one
invocation and is how hooks pass values to each other; `order()` fixes the sequence, lowest first.

Every `SessionHook` bean in the application context takes part — there is no `type` setting to select one.

### `org.owasp.oag.services.blacklist.SessionBlacklist`

Keeps track of session identifiers that must no longer be accepted. Extends `java.io.Closeable`.

```java
void invalidateSession(String identifier, int ttl);
boolean isInvalidated(String identifier);
void cleanup();
```

`invalidateSession` is called on logout and must keep the identifier blacklisted for at least `ttl`
seconds. `isInvalidated` is called by the session-extraction servlet filter for every request that presents
a session cookie. `cleanup` drops expired entries; OAG schedules it on a background thread every two
session durations (`sessionBehaviour.sessionDuration`), so an implementation must be safe to call
concurrently with `invalidateSession` and `isInvalidated`, and must not lose an identifier that is
invalidated while a cleanup pass is running. All three are blocking calls.

### `org.owasp.oag.services.login.drivers.LoginDriver`

Implements authentication against one identity provider.

```java
LoginDriverResult startLogin(URI callbackUri);
UserModel processCallback(HttpServletRequest request, String state, URI callbackUri) throws AuthenticationException;
URL processFederatedLogout(UserModel userModel);
List<String> getSettingsErrors(LoginProviderSettings settings);
default boolean settingsAreValid(LoginProviderSettings settings);
```

`startLogin` is called when a client hits `/auth/{provider}/login` and returns the redirect to the provider
plus the generated `state`. `processCallback` is called when the provider redirects back to
`/auth/{provider}/callback`; it validates `state`, exchanges the code for tokens and returns the
`org.owasp.oag.session.UserModel` the session is built from. `processFederatedLogout` is called on logout
and may return a URL to redirect to so the session also ends at the provider, or `null` to use
`redirectLogout`. `getSettingsErrors` is called at startup to validate the configuration.

A driver is created by a `org.owasp.oag.services.login.drivers.LoginDriverFactory<T>`, selected through the
login provider's `type` value.

### `org.owasp.oag.logging.TraceContext`

Produces and propagates the trace id (correlation id) of a request.

```java
void generateNewTraceId();
void applyExistingTrace(String primaryTraceInfo, String secondaryTraceInfo);
boolean sendTraceDownstream();
boolean forwardIncomingTrace();
boolean acceptAdditionalTraceInfo();
boolean sendTraceResponse();
String getMainRequestHeader();
String getSecondaryRequestHeader();
String getResponseHeader();
String getTraceString();
String getSecondaryTraceInfoString();
String getTraceResponseString();
```

A new instance is created per request by
`org.owasp.oag.infrastructure.factories.TraceContextFactory.createContextForRequest()`. The tracing servlet
filter then calls either `applyExistingTrace` (when a trace header arrives and `forwardIncomingTrace` is
set) or `generateNewTraceId`, and reads the header names and values from the remaining methods to propagate
the trace downstream and, optionally, back upstream. The getters throw
`org.owasp.oag.exception.TraceException` when no trace has been set up.

Selected through the `traceProfile` `type` value.

### `org.owasp.oag.services.crypto.jwt.JwtSignerFactory`

Creates the signer that signs downstream user-mapping JWTs.

```java
String JWT_SIGNER_FACTORY_BEAN_POSTFIX = JwtSignerFactory.class.getSimpleName();
JwtSigner create(String hostUri, Map<String, Object> settings);
```

Called by the JWT user mapping when it initialises, once per security profile. `settings` is the raw
user-mapping settings map from the configuration file. The returned
`org.owasp.oag.services.crypto.jwt.JwtSigner` is an abstract class responsible for signing and for its own
key handling.

The bean name is the `signatureImplementation` value plus `JWT_SIGNER_FACTORY_BEAN_POSTFIX`, so
`signatureImplementation: rsa` resolves the bean named `rsaJwtSignerFactory`.

### `org.owasp.oag.services.keymgm.JwkStore`

Holds OAG's JWT signing keys and serves the public ones.

```java
JWKSet getSigningPublicKey(String kid);
JWKSet getSigningPublicKeys();
void addRawKey(String kid, Key key, long expiry);
void addJwk(JWK key, long expiry);
void removeKey(String kid);
```

`JWKSet` and `JWK` are the Nimbus types `com.nimbusds.jose.jwk.*` and `Key` is `java.security.Key`. The
`add*` methods are called by key rotation whenever a new signing key is generated, `removeKey` when a key
has expired, and the two getters back the `/.well-known/jwks` and `/.well-known/jwks/{kid}` endpoints.
`expiry` is a Unix timestamp.

Selected through `keyManagementProfile.jwkStoreProfile.type` and retrieved via
`org.owasp.oag.infrastructure.factories.KeyManagementFactory.getJWKStore()`.

### `org.owasp.oag.services.keymgm.KeyGenerator`

Creates the keys used to sign JWTs.

```java
GeneratedKey generateJWTSigningKey();
```

Called by key rotation at startup and on every rotation interval. `GeneratedKey` is the nested class
`org.owasp.oag.services.keymgm.KeyGenerator.GeneratedKey`; it wraps either a `java.security.KeyPair`
(asymmetric signing, e.g. RS256) or a single `java.security.Key` (symmetric signing, e.g. HS256), and
exposes `getKeyUsedToVerify()`, `getTypeString()` and `equalType(GeneratedKey)`.

Selected through `keyManagementProfile.keyGeneratorProfile.type` and retrieved via
`org.owasp.oag.infrastructure.factories.KeyManagementFactory.getKeyGenerator()`.
