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

- **A. Configuration validation (cycles 1–5).** Config POJOs only validate their own structure and no longer
  ask the application context. Invert the dependency:
  - Add `ConfigValidator { List<String> validate(MainConfig config); }` in `config`.
  - Each extension point provides a validator next to its registry (e.g. `LoginProviderValidator` in
    `services.login.drivers` checks type and driver settings).
  - `StartupEventListeners` collects all `ConfigValidator` beans and runs them after `config.getErrors()`.
  - `ErrorValidation.getErrors(ApplicationContext)` becomes `getErrors()`. This removes the `context == null`
    guards and the `getErrors(null)` calls in tests.
  - Afterwards all edges run `services → config`.
  - Cheaper variant: each registry implements `ConfigValidator` itself (no new classes, but mixes two
    responsibilities). Recommended: separate validators. **Needs a decision.**
- **B. `config` ↔ `config.configuration`.** Move `ErrorValidation` into `config.configuration`, the only
  package that implements it.
- **C. root ↔ `infrastructure`.** Use `StartupEventListeners.class` as the logger name.
- **D. `infrastructure` ↔ `services.blacklist`.** Move `GlobalClockSource` (a leaf utility used by
  `filters.servlet`, `hooks.session`, `services.blacklist`, `services.tokenMapping.jwt`) to a dependency-free
  package such as `org.owasp.oag.clock` or `utils`. Alternative: move the `sessionBlacklist` bean into a
  configuration class in `services.blacklist`.
- **E. `cookies` ↔ `session`.** A session should not know its cookie representation:
  - `Session` keeps the CSRF token instead of the whole `LoginCookie`.
  - `CsrfCookieCreationHook` reads the token from the session.
  - `fromSessionCookie` moves to the cookie side (e.g. `LoginCookie.toSession(clock)`).
- **F. `filters.servlet` ↔ `hooks.session`.** Delete the unused import in `AddSessionToBlacklistHook`.
  Implement `00006` Task 7: move `findCookie` to `cookies.CookieReader`, which unhooks `services.csrf` from the
  filters.
- **G. `filters.proxy` ↔ `gateway`.** Move `ATTRIBUTE_ROUTE_NAME` to `RouteAwareFilter`, the package that reads it.
- **H. Keep it fixed.** Add an ArchUnit test (test dependency only):
  `slices().matching("org.owasp.oag.(**)").should().beFreeOfCycles()`. It blocks new cycles in CI.

### Suggested order

| Step | Content | Effort | Risk |
|---|---|---|---|
| 1 | C, F (unused import), G, B | minutes each | none |
| 2 | D, F (`CookieReader`) | small | low |
| 3 | E (session/cookie split) | medium | touches session handling, needs careful tests |
| 4 | A (config validators) | medium to large | changes the validation API and many tests |
| 5 | H (ArchUnit test) | small | add once cycles are gone, or start with a list of known exceptions |

Steps 1 and 2 alone remove 5 of the 11 cycles without behaviour change.

### Open questions

- [ ] A: separate `ConfigValidator`s per extension point (recommended) or registries that validate?
- [ ] One new iteration plan (`00008`) or an extra step on the `00007` branch before merging?

## Other open items from iteration 00007

- [ ] Run `npm run docs:build` in `www/` once (npm was not available when the docs were changed).
- [ ] Merge `feature/00007-extension-registries` (not merged; never commit to `main` directly).
