# Separate session and login cookie

**Status:** finished

**Goal:** Remove cycle E (`cookies` ↔ `session`), the last package cycle. `Session` keeps the CSRF token
instead of the whole `LoginCookie`, and the conversion from cookie to session moves to `LoginCookie`. Design
and rationale: `todo.md`, section "E in detail".

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Decisions

1. `Session`: field, getter and constructor parameter `LoginCookie loginCookie` become `String csrfToken` /
   `getCsrfToken()`, same constructor position.
2. `Session.fromSessionCookie(LoginCookie, Clock)` becomes `LoginCookie.toSession(Clock)`, which returns
   `Optional<Session>`, empty when expired. The logic is unchanged.
3. `CsrfCookieCreationHook.renewSession` reads `session.getCsrfToken()`.
4. `CsrfCookie.REQUEST_CSRF_TOKEN` is not changed in this iteration.
5. `PackageCycleTest`: once no cycle is left, the known-back-edge mechanism is removed. The plain rule plus
   the root package rule remain. A new cycle has to be fixed, not listed.
6. Breaking change for custom extensions that call `Session.getLoginCookie()` or `Session.fromSessionCookie`.

## Review findings incorporated

- The baseline is red on purpose: `PackageCycleTest` already runs the plain rule (exception list disabled),
  so step 3 only removes the dead exception code.
- No other cycle remains; the only `session → cookies` edge is the `LoginCookie` import in `Session`.
- The JSON of `LoginCookie` stays the same: `JweEncrypter` uses a plain `ObjectMapper`, and `toSession` is not a
  getter.
- `SessionRenewalTest` does not check the CSRF token yet. The new assertion decrypts the old and the renewed
  session cookie with `CookieConverter` and compares their CSRF tokens.
- `Setup-for-OAG-development.md` already describes no exceptions; nothing to change there.

## Steps

### 0. Preparation
- [x] Baseline `./gradlew build`: 194 tests, only `packagesAreFreeOfCycles` fails (cycle E; exception list disabled).

### 1. Cookie to session
- [x] Unit `LoginCookieTest`: `toSession` copies all values including the CSRF token; an expired cookie gives
      an empty result; the JSON form has no `session` property.
- [x] Add `LoginCookie.toSession`; switch `ExtractAuthenticationFilter`; delete `Session.fromSessionCookie`.
      (Steps 1 and 2 were implemented together; `toSession` builds the session with the CSRF token directly.)

### 2. Session holds the CSRF token
- [x] `ExtractAuthenticationFilterTest`: the session carries the CSRF token of the cookie.
- [x] Replace `loginCookie` by `csrfToken` in `Session`; switch `CsrfCookieCreationHook`; adapt the tests.
- [x] `SessionRenewalTest`: the renewed session cookie carries the CSRF token of the old one (decrypted with
      `CookieConverter`). Verified that the test fails when the hook drops the token.

### 3. Cycle test
- [x] `PackageCycleTest`: plain cycle rule and root package rule; remove `KNOWN_BACK_EDGES`, `KnownBackEdge`
      and `knownBackEdgesStillExist`.
- [x] `./gradlew build` fully green: 197 tests, 0 failures, `requireJavadoc` passes.

### 4. Documentation
- [x] `Custom-(code)-extensions.md`: mention `Session.getCsrfToken()`.
- [x] `todo.md`: E done.
- [x] Mark this plan finished.
