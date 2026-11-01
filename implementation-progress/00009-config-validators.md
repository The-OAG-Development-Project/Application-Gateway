# Configuration validation through per-extension validators

**Status:** finished

**Goal:** Suggestion A from `todo.md`. Configuration POJOs in `config.configuration` validate only their own
structure. Extension-type checks move to one `ConfigValidator` per extension point, located next to its
registry. This removes the five back edges `config.configuration → services.login.drivers`, `services.csrf`,
`services.keymgm`, `services.tokenMapping`, `logging`.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Decisions

1. **Separate validators** (decided by the user), not registries that validate themselves.
2. `ErrorValidation.getErrors()` has no parameter. Structure checks stay in the POJOs.
3. `ConfigValidator { List<String> validate(MainConfig config); }` lives in `config.configuration`.
4. One `@Component` validator per extension point, next to its registry:

   | Validator | Package | Checks |
   |---|---|---|
   | `LoginProviderValidator` | `services.login.drivers` | per login provider: type known, else load driver and collect setting errors |
   | `CsrfProtectionValidator` | `services.csrf` | per security profile: `csrfProtection` known |
   | `UserMappingValidator` | `services.tokenMapping` | per security profile: `userMapping.type` known |
   | `KeyManagementValidator` | `services.keymgm` | JWK store, key generator and key rotation types known |
   | `TraceProfileValidator` | `logging` | `traceProfile.type` known |

5. `TypeRegistry.unknownTypeError(configEntry, type)` returns `Optional<String>` with the common message
   `<configEntry>: Unknown type '<type>'. Known types: [..]`.
6. `MainConfigValidator` (`config`, `@Component`) runs `config.getErrors()` and, only when that is empty, all
   `ConfigValidator`s. `StartupEventListeners` uses it and no longer needs `ApplicationContext`.
7. Out of scope: beans that resolve their type in the constructor (`SecurityProfileUserMappers`,
   `DefaultKeyRotation`, `JwksController`, `TraceContextBridge`) still fail before validation runs.

## Review findings incorporated

- `MainConfigValidator.validate(MainConfig)` takes the config as a parameter (tests validate configs loaded
  from files).
- A missing `userMapping.type` is a structure error in `UserMappingConfig`. Validators skip `null` types, so no
  "Unknown type 'null'" error appears in addition.
- Error order: an unknown type is now reported only after the cross-cutting checks in `MainConfig` (for example
  a route referencing a missing security profile) pass. `MainConfigValidatorTest` covers this.
- The trace error keeps the hint "Specify 'noTraceContext' to disable correlation logging."
- `LoginProviderValidator` keeps the catch of other `RuntimeException`s from creating the driver.
- Validators keep the "Using X implementation" info logs.
- `PackageCycleTest` currently has the exception list and `knownBackEdgesStillExist` switched off on purpose
  (by the user, to see all cycles). This iteration removes the five entries but does not switch the list back
  on, so that test stays red until D and E are done.

## Steps

### 0. Preparation
- [x] Baseline `./gradlew build`: 175 tests, only `PackageCycleTest.packagesAreFreeOfCycles` fails (exception list switched off on purpose).

### 1. Infrastructure
- [x] Unit: `TypeRegistryTest` for `unknownTypeError`; `MainConfigValidatorTest` (structure errors stop the
      validators; otherwise errors of all validators are concatenated).
- [x] Add `ConfigValidator`, `MainConfigValidator`, `TypeRegistry.unknownTypeError`. Switch
      `StartupEventListeners`.

### 2. Validators (one extension point each, build green after each)
- [x] Trace: `TraceProfileValidator` + unit test; remove registry lookup from `TraceProfile`.
- [x] Key management: `KeyManagementValidator` + unit test; remove lookups from the three profiles.
- [x] CSRF: `CsrfProtectionValidator` + unit test; remove lookup from `SecurityProfile`.
- [x] User mapping: `UserMappingValidator` + unit test; remove lookup from `UserMappingConfig`; missing `type`
      is always reported (unit test).
- [x] Login: `LoginProviderValidator` + unit test (unknown type, invalid settings with the real
      `OidcLoginDriverFactory`, valid settings); remove lookup from `LoginProvider`. Replace
      `LoginProvidorValidationTest`.

### 3. API cleanup
- [x] `ErrorValidation.getErrors()` without `ApplicationContext`; adapt all 12 implementations and the tests
      (`getErrors(null)` → `getErrors()`); remove the redundant check at the end of `KeyRotationProfile`.

### 4. Integration
- [x] `ExtensionTypeValidationTest` validates `sample-config.yaml` and the test YAML files through
      `MainConfigValidator`. Its unknown-type tests stay as integration tests: they change one type in
      `sample-config.yaml` and validate through `MainConfigValidator` with the real validator beans.
- [x] Startup: `StartupEventListenersTest` (unit) verifies that validation errors stop startup. A full
      application start with a bad configuration was dropped: `OAGBeanConfiguration`'s component scan also
      registers nested test configurations of other tests, so such a start cannot be isolated (recorded in
      `todo.md`).
- [x] `PackageCycleTest`: remove the five `config.configuration` entries from `KNOWN_BACK_EDGES`. Verified with the exception list temporarily switched on: no cycle left apart from D and E.
- [x] `./gradlew build`: 194 tests, only `PackageCycleTest.packagesAreFreeOfCycles` fails (exception list switched off on purpose); `requireJavadoc` passes.

### 5. Documentation
- [x] `www/docs/docs/Custom-(code)-extensions.md`: how configured types are validated at startup.
- [x] `todo.md`: mark A done.
- [x] Mark this plan finished.
