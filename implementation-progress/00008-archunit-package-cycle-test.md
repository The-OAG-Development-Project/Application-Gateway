# ArchUnit test against package cycles

**Status:** finished

**Goal:** Suggestion H from `todo.md`: a test that fails the build when a new package cycle appears in
`org.owasp.oag`. The cycles that still exist are listed as explicit, documented exceptions, so the test is
green today and the list can only shrink.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Decisions

1. **Library:** `com.tngtech.archunit:archunit-junit5` as `testImplementation` only.
2. **Rule:** `slices().matching("org.owasp.oag.(**)").should().beFreeOfCycles()` on production classes
   (`ImportOption.DoNotIncludeTests`). Every package is its own slice.
3. **Known cycles as ignored back edges, not `FreezingArchRule`.** A frozen violation store is a generated
   text file that nobody reads. Instead the test lists each remaining back edge (origin package → target
   package) with the `todo.md` suggestion that removes it, and ignores exactly that edge. The edges are
   matched by exact, fully qualified package name, so a new dependency from or to a subpackage is still
   checked. Accepted limit: an ignored edge ignores every class-level dependency between that package pair.
4. **Exceptions must stay real.** A second test fails when a listed back edge no longer exists in the code, so
   whoever removes a cycle also removes its exception.
5. **Root package.** `org.owasp.oag.(**)` does not match the root package `org.owasp.oag` itself. A separate
   rule forbids subpackages to depend on the root package, so no cycle can run through it.
6. **Unit test** in `oag/src/test/java/org/owasp/oag/architecture/` (only reads compiled classes, no other
   process).

## Known back edges (state at the start of this iteration)

| Origin → target | Cause | Removed by |
|---|---|---|
| `config.configuration` → `config` | config classes implement `ErrorValidation` | B |
| `config.configuration` → `services.login.drivers` | `LoginProvider.getErrors` uses `LoginDriverRegistry` | A |
| `config.configuration` → `services.csrf` | `SecurityProfile.getErrors` uses `CsrfValidationRegistry` | A |
| `config.configuration` → `services.keymgm` | key profiles use the key registries | A |
| `config.configuration` → `services.tokenMapping` | `UserMappingConfig.getErrors` uses `UserMappingRegistry` | A |
| `config.configuration` → `logging` | `TraceProfile.getErrors` uses `TraceContextRegistry` | A |
| `services.blacklist` → `infrastructure` | `LocalPersistentBlacklist` uses `GlobalClockSource` | D |
| `session` → `cookies` | `Session` holds a `LoginCookie` | E |

No further edges were needed. The root package rule found one more cycle,
`org.owasp.oag` → `infrastructure` → `config` → `org.owasp.oag`: `FileConfigLoader` used
`OWASPApplicationGatewayApplication.class` only to load `/default-config.yaml`. It now uses its own class
(absolute resource path, same class loader), which removes the cycle.

## Review findings incorporated

- Exact, fully qualified package names in ignored edges; accepted limit documented (decision 3).
- Root package blind spot closed by a separate rule (decision 5).
- Default ArchUnit cycle report limits (100 cycles) are enough; no `archunit.properties`.
- Known edges are one list of records; the rule and the "edge still exists" test are both built from it.
- Glossary: not needed, the terms are developer-internal and explained in the development docs.

## Steps

- [x] Add the ArchUnit test dependency.
- [x] Red: plain `beFreeOfCycles()` rule fails (33 cycle reports).
- [x] Add the known back edges as ignored dependencies; rule is green.
- [x] Test that every known back edge still exists.
- [x] Rule: no subpackage depends on the root package `org.owasp.oag` (fixed `FileConfigLoader`).
- [x] Verify that the rule catches a new cycle (temporary dependency `services.blacklist` → `hooks.session` reported as a cycle, then reverted).
- [x] `./gradlew build` green (176 tests, 0 failures).
- [x] Update `todo.md` (H done) and `www/docs/docs/Setup-for-OAG-development.md` (cycle test, how to shrink the list).
- [x] Mark this plan finished.
