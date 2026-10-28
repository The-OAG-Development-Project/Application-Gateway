# Move OAGBeanConfiguration to the root package

**Status:** finished

**Goal:** Remove cycle D (`infrastructure` ↔ `services.blacklist`) from `todo.md`. `OAGBeanConfiguration` is
the only class in `infrastructure` that depends on `services.blacklist` (it creates the `sessionBlacklist`
bean). It is the composition root of OAG and moves to `org.owasp.oag`, next to its only user
`EnableOWASPApplicationGateway`.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Decisions

1. Target package is the root `org.owasp.oag`. No subpackage depends on the root (guarded by
   `PackageCycleTest.subpackagesDoNotDependOnRootPackage`), so the root may depend on every package.
2. The class keeps its name, content and `@ComponentScan` list. `org.owasp.oag.infrastructure` stays in the
   scan list for `GlobalClockSource`, `SchedulerConfiguration` and `StartupEventListeners`.
3. `GlobalClockSource` stays in `infrastructure`; moving it is optional tidying, not needed for the cycle.
4. Breaking change for code that imports `org.owasp.oag.infrastructure.OAGBeanConfiguration` directly.
   The documented entry point `@EnableOWASPApplicationGateway` is unchanged.

## Review findings incorporated

- No Spring behaviour change: the class was already found by the `@SpringBootApplication` scan and by
  `@Import`; Spring registers it once. The package-private `configLoader` bean method is only used inside the
  class.
- No test or documentation page references the class. `infrastructure/package-info.java` is updated.

## Steps

- [x] Red: remove the D entry from `KNOWN_BACK_EDGES`; with the exception list switched on temporarily, the
      cycle rule reports `infrastructure` ↔ `services.blacklist`.
- [x] Move `OAGBeanConfiguration` to `org.owasp.oag`; update `EnableOWASPApplicationGateway`.
- [x] Green: cycle rule passes with only the E entry (exception list switched on temporarily, then restored
      to the user's state).
- [x] `./gradlew build`: 194 tests, only the intentionally disabled `packagesAreFreeOfCycles` fails; `requireJavadoc` passes.
- [x] Docs: no references in `www/docs`; `infrastructure/package-info.java` updated; `todo.md` marks D done.
- [x] Mark this plan finished.
