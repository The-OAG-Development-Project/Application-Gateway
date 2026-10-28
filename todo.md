# TODO

## Resolve package cycles

Analysis of `oag/src/main` after iteration `00007` (branch `feature/00007-extension-registries`), based on a
package-level import graph.

**Result:** 11 two-package cycles. Together they make 15 packages mutually reachable: root, `config`,
`config.configuration`, `cookies`, `filters`, `filters.servlet`, `hooks.session`, `infrastructure`, `logging`,
`services.blacklist`, `services.csrf`, `services.keymgm`, `services.login.drivers`, `services.tokenMapping`,
`session`. A separate 2-package cycle exists between `filters.proxy` and `gateway`.

### Findings

| # | Cycle | One direction | Back edge (the problem) |
|---|---|---|---|
| 1–5 | `config.configuration` ↔ `services.login.drivers`, `services.csrf`, `services.keymgm`, `services.tokenMapping`, `logging` | Extensions import `MainConfig` or settings classes (the natural direction) | Config POJOs validate their types in `getErrors(context)`: `LoginProvider → LoginDriverRegistry`, `SecurityProfile → CsrfValidationRegistry`, `JwkStoreProfile`/`KeyGeneratorProfile`/`KeyRotationProfile → *Registry`, `UserMappingConfig → UserMappingRegistry`, `TraceProfile → TraceContextRegistry` |
| 6 | `config` ↔ `config.configuration` | `ConfigLoader`/`FileConfigLoader → MainConfig` | 12 config classes implement `config.ErrorValidation` |
| 7 | root ↔ `infrastructure` | `EnableOWASPApplicationGateway → OAGBeanConfiguration` | `StartupEventListeners` uses `OWASPApplicationGatewayApplication.class` only as its logger name |
| 8 | `infrastructure` ↔ `services.blacklist` | `OAGBeanConfiguration` creates the blacklist bean | `LocalPersistentBlacklist → GlobalClockSource` |
| 9 | `cookies` ↔ `session` | `LoginCookie → UserModel` | `Session` holds a `LoginCookie` and builds itself from one (`fromSessionCookie`); the only outside caller of `getLoginCookie()` is `CsrfCookieCreationHook`, which only reads the CSRF token |
| 10 | `filters.servlet` ↔ `hooks.session` | `SessionRenewalFilter → SessionHookChain` | `AddSessionToBlacklistHook` imports `ExtractAuthenticationFilter` but never uses it |
| 11 | `filters.proxy` ↔ `gateway` | `RouteAwareFilter` reads `GatewayRouterFunctionConfiguration.ATTRIBUTE_ROUTE_NAME` | The gateway configuration collects all `RouteAwareFilter`s |

Additional edge: `CsrfSameSiteStrictCookieValidation` calls `ExtractAuthenticationFilter.findCookie`, so a
service depends on a servlet filter. Moving it to `cookies.CookieReader` was planned as Task 7 of iteration
`00006` but was not implemented.

Compared with `main`, iteration `00007` removed three cycles (all through `infrastructure.factories`) and added
cycles 3–5. Cycles 1 and 2 follow the same pattern and already existed on `main`.

### Suggestions

- **D. `infrastructure` ↔ `services.blacklist`.** Done (iteration `00010`): `OAGBeanConfiguration`, the only
  class in `infrastructure` that depended on `services.blacklist`, moved to the root package `org.owasp.oag`.
  Optional tidying: move `GlobalClockSource` to its own leaf package.
- **E. `cookies` ↔ `session`.** A session should not know its cookie representation:
  - `Session` keeps the CSRF token instead of the whole `LoginCookie`.
  - `CsrfCookieCreationHook` reads the token from the session.
  - `fromSessionCookie` moves to the cookie side (e.g. `LoginCookie.toSession(clock)`).
- ?? what was this:
  Implement `00006` Task 7: move `findCookie` to `cookies.CookieReader`, which unhooks `services.csrf` from the
  filters.

### Suggested order

| Step | Content | Effort | Risk |
|---|---|---|---|
| 1 | C, F (unused import), G, B | minutes each | none |
| 2 | ~~D~~ done, F (`CookieReader`) | small | low |
| 3 | E (session/cookie split) | medium | touches session handling, needs careful tests |


Steps 1 and 2 alone remove 5 of the 11 cycles without behaviour change.

