# Read request cookies in the cookies package

**Status:** finished

**Goal:** Suggestion F from `todo.md`. Move the static helper `ExtractAuthenticationFilter.findCookie` to a new
`org.owasp.oag.cookies.CookieReader`. Afterwards no class outside `filters.servlet` depends on a servlet filter,
and an ArchUnit rule keeps it that way.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Current state (problem)

`findCookie(HttpServletRequest, String)` is a general cookie lookup, but it lives in a servlet filter. Other
packages reach into the filter to use it:

| Caller | Package | Cookie |
|---|---|---|
| `ExtractAuthenticationFilter.extractLoginCookieFromRequest` | `filters.servlet` | `LoginCookie.NAME` |
| `CsrfSameSiteStrictCookieValidation.extractCsrfToken` | `services.csrf` | `CsrfCookie.NAME` |
| `LoginController.loadLoginState` | `controllers` | `LoginStateCookie.NAME` |

As a result, a service and a controller depend on a filter, against the layering. It is not a package cycle.

Two more classes import a servlet filter without using it. Imports are not in the bytecode, so this is no
real dependency and ArchUnit does not see it; it is source noise only:

| Class | Unused import |
|---|---|
| `filters.proxy.CsrfValidationFilterWithBody` | `ExtractAuthenticationFilter` |
| `filters.proxy.DownstreamHeaderFilter` | `TraceContextFilter` |

`cookies` already has the write side: `CookieWriter.addCookie(HttpServletResponse, ResponseCookie)`.

## Decisions

1. **`CookieReader`** in `org.owasp.oag.cookies`. It mirrors `CookieWriter`: a `final` class with a private
   constructor and one static method, `findCookie(HttpServletRequest request, String name)` returning
   `Optional<Cookie>`. The signature and behaviour stay exactly as they are: the first cookie with that name,
   and empty when the request has no cookies.
2. **Remove `ExtractAuthenticationFilter.findCookie`; don't keep a delegating copy.** It's a public static
   helper, not a documented extension point; no doc page mentions it.
3. **A static method, not a bean.** It has no state or dependencies, like `CookieWriter`. Moving it into
   `CookieConverter` was considered, but then a caller would need a bean just to find a cookie by name, and
   `CookieConverter` would mix lookup with conversion.
4. **ArchUnit guard** in a new `architecture.LayeringTest` (class Javadoc, `@AnalyzeClasses` with
   `ImportOption.DoNotIncludeTests` like `PackageCycleTest`, Javadoc on the rule field): no class outside
   `org.owasp.oag.filters.servlet..` depends on classes in `org.owasp.oag.filters.servlet..`. Servlet filters are
   wired by component scan (the root package only names `"org.owasp.oag.filters"` as a string), so nothing needs
   to reference them. A separate class keeps `PackageCycleTest`'s Javadoc ("against package cycles") accurate.
5. **Out of scope:** the other half of `00006` Task 7 (moving the handler-mapping order `BeanPostProcessor`
   out of `GatewayRouterFunctionConfiguration`). It has nothing to do with cookies; it stays open and goes into
   `todo.md` as its own item.

## Review findings incorporated

- Callers, unused imports (no `{@link}` either) and the absence of other references to `filters.servlet` were
  confirmed; Javadoc mentions elsewhere are plain text or `{@code}`.
- ArchUnit does not see unused imports, so the red list has 2 classes, not 4; removing the imports is cleanup.
- The rule goes into a separate `LayeringTest` and uses `filters.servlet..` to cover future subpackages.
- `cookies/package-info.java` does not exist; that doc step is dropped. The rule is documented in
  `Setup-for-OAG-development.md`.
- `CookieReaderTest` gets a case-sensitivity case, since the behaviour must stay the same.

## Steps

### 0. Preparation
- [x] Baseline `./gradlew build`: 212 tests, 0 failures.

### 1. Guard first (red)
- [x] ArchUnit rule from decision 4. It fails and names exactly 2 classes: `CsrfSameSiteStrictCookieValidation`
      and `LoginController`. The build stays red until step 2; do not commit in between.

### 2. CookieReader
- [x] Unit `CookieReaderTest` (in `test/.../cookies`, with `MockHttpServletRequest`): the cookie with the
      requested name is returned; the first one wins when the name occurs twice; a request with other cookies
      only gives empty; a request without cookies gives empty; the name is matched case-sensitively.
- [x] Add `CookieReader` (Javadoc on class and method).
- [x] Switch `ExtractAuthenticationFilter`, `CsrfSameSiteStrictCookieValidation` and `LoginController`; remove
      `ExtractAuthenticationFilter.findCookie` and its now unused imports (`Arrays`, `jakarta.servlet.http.Cookie`).
      `LoginController` keeps its `Cookie` import.
- [x] Cleanup (not required by the rule): remove the unused imports in `CsrfValidationFilterWithBody` and
      `DownstreamHeaderFilter`.

### 3. Verify
- [x] The ArchUnit rule is green.
- [x] Existing tests that cover the three callers stay green: `ExtractAuthenticationFilterTest`,
      `CsrfSameSiteStrictCookieValidationTest`, `CsrfSamesiteStrictTest`, `CsrfLogoutTest`, `LoginLogoutTest`.
- [x] `./gradlew build` green, including `requireJavadoc`: 218 tests, 0 failures.

### 4. Documentation
- [x] `Setup-for-OAG-development.md`, section "Package cycles": describe `LayeringTest` and its rule as it holds
      (no history wording). No page mentions `findCookie`. Glossary: no change expected.
- [x] `todo.md`: mark F done and update the "Additional edge" note in the findings; add the handler-mapping
      order configuration as a separate open item.
- [x] Mark this plan finished.

## Risks

- Breaking change only for custom code that calls `ExtractAuthenticationFilter.findCookie`; it switches to
  `CookieReader.findCookie`. Not a documented API.
- No behaviour change: the method body moves unchanged.
