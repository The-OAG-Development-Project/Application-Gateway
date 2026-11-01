# Read the session's CSRF token from the session

**Status:** finished

**Goal:** Remove the scoped value `CsrfCookie.REQUEST_CSRF_TOKEN`. CSRF validations read the token bound to
the session with `Session.getCsrfToken()` (available since iteration `00011`). This removes a duplicate of
the session state and fixes a mismatch between code and documentation.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Current state (problem)

`ExtractAuthenticationFilter` binds two scoped values from the same decrypted login cookie:

| Cookie state | `Session.REQUEST_SESSION` | `CsrfCookie.REQUEST_CSRF_TOKEN` |
|---|---|---|
| no or undecryptable cookie | empty | `null` |
| blacklisted | empty | `null` |
| expired, not blacklisted | empty | **token of the expired cookie** |
| valid | session (carries the token since `00011`) | token (same value as `session.getCsrfToken()`) |

- The documentation (`Custom-(code)-extensions.md`, `Architecture-Request-Handling.md`) says the token is
  `null` whenever there is no usable session. The expired row contradicts this.
- No security impact today: all three built-in validations (`CsrfDoubleSubmitCookieValidation`,
  `CsrfDoubleSubmitCookieWithBodyValidation`, `CsrfSameSiteStrictCookieValidation`) read the token only inside
  `if (sessionOptional.isPresent())`. A custom extension that reads `REQUEST_CSRF_TOKEN` without checking the
  session could, however, accept the token of an expired session.
- When a session is present, both values are equal, so `REQUEST_CSRF_TOKEN` is redundant.

Users of `REQUEST_CSRF_TOKEN`:

| Kind | Where |
|---|---|
| binds | `filters.servlet.ExtractAuthenticationFilter:96` |
| reads | `services.csrf.CsrfDoubleSubmitCookieValidation:72`, `CsrfDoubleSubmitCookieWithBodyValidation:51`, `CsrfSameSiteStrictCookieValidation:71` |
| tests | `ExtractAuthenticationFilterTest:78`, `CsrfDoubleSubmitCookieValidationTest:32/42/53`, `GatewayRouterFunctionConfigurationTest:49` |
| docs | `Custom-(code)-extensions.md:103,107,137`, `Architecture-Request-Handling.md:55` |

## Decisions

1. **Remove `REQUEST_CSRF_TOKEN`; do not deprecate it first.** It is the only way to read the token outside the
   session, so keeping it would keep the inconsistency. The extension API already broke in `00007` and `00011`
   on this branch, so one more change before the merge costs little. Alternative, if a deprecation period is
   wanted: keep the field `@Deprecated`, bind it only for a valid session, and remove it in a later release.
2. **Validations read `session.getCsrfToken()`** from the session they already take from `REQUEST_SESSION`.
   Behaviour of the built-in validations does not change: they only act when a session is present, and in
   that case both values are equal.
3. **A session without a CSRF token** (`getCsrfToken() == null`) is handled exactly as today, because today
   the scoped value is `null` in the same case. Tests pin this per validation.
4. `CsrfCookie` keeps `NAME` and the cookie value class; only the scoped value goes.

## Review findings incorporated

- State table, behaviour neutrality (including `null` tokens and the body/header/query fallback of
  `CsrfDoubleSubmitCookieWithBodyValidation`) and the list of users were confirmed against the code.
- The session helper in `CsrfDoubleSubmitCookieValidationTest` passes a `null` token; it must carry the token
  **before** the validations are switched, otherwise the existing tests fail.
- `CsrfSameSiteStrictCookieValidationTest` only covers the origin helper today. `shouldBlockRequest` needs a
  `CookieConverter` and `MainConfig`: use a real `CookieConverter` with `JweEncrypter.loadInMemoryInstance()`
  and a plain `MainConfig` (the project uses no mocking library).
- Logout uses `CsrfSameSiteStrictCookieValidation` through `LoginController`; `CsrfLogoutTest` covers it.

## Steps

### 0. Preparation
- [x] Baseline `./gradlew build`: 197 tests, 0 failures.

### 1. Validations read the token from the session (behaviour unchanged)
- [x] `CsrfDoubleSubmitCookieValidationTest`: the session helper carries `CSRF_TOKEN`.
- [x] Unit tests, written first, that bind only `REQUEST_SESSION` with a session carrying the token
      (no `REQUEST_CSRF_TOKEN`). Per built-in validation: matching token passes, wrong token blocks, session
      without token blocks, no session passes.
  - `CsrfDoubleSubmitCookieValidationTest`: extend.
  - `CsrfDoubleSubmitCookieWithBodyValidationTest` (new): additionally, token in the body passes and a
    `null` body throws `AssertionError`.
  - `CsrfSameSiteStrictCookieValidationTest`: extend with `shouldBlockRequest` (real `CookieConverter`, see
    review findings).
- [x] Switch the three validations to `session.getCsrfToken()`.
- [x] Remove `REQUEST_CSRF_TOKEN` from `CsrfDoubleSubmitCookieValidationTest` and
      `GatewayRouterFunctionConfigurationTest`. (Red first: 11 of 22 CSRF unit tests failed before the switch.)

### 2. Remove the scoped value
- [x] `ExtractAuthenticationFilterTest`: the token is checked via the session only; new test: an expired, not
      blacklisted cookie gives an empty session (pins the row that was inconsistent).
- [x] Remove the binding in `ExtractAuthenticationFilter` and the field `CsrfCookie.REQUEST_CSRF_TOKEN`.
- [x] Integration: the existing CSRF tests (`CsrfDoubleSubmitTest`, `CsrfSamesiteStrictTest`,
      `CsrfLogoutTest`, `CsrfNoneSubmitTest`) stay green.
- [x] `./gradlew build` green: 212 tests, 0 failures, `requireJavadoc` passes.

### 3. Documentation
- [x] `Custom-(code)-extensions.md`: remove `REQUEST_CSRF_TOKEN` from "Reading the state of the current
      request"; the `CsrfProtectionValidation` section reads the token with `session.getCsrfToken()`.
- [x] `Architecture-Request-Handling.md`: remove the `REQUEST_CSRF_TOKEN` row from the scoped-value table.
- [x] Glossary: "Scoped value" no longer lists the CSRF token.
- [x] `todo.md`: remove the "Found on the way" note and the other mentions of `REQUEST_CSRF_TOKEN`.
- [x] Mark this plan finished.

## Risks

- Breaking change for custom extensions that read `CsrfCookie.REQUEST_CSRF_TOKEN`: they must read
  `Session.REQUEST_SESSION.get().map(Session::getCsrfToken)` instead. This needs a note in the release notes
  of the next version.
- No change to the cookie format, the CSRF cookie, or the HTTP behaviour.
