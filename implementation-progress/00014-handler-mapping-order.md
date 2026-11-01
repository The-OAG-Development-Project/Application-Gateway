# Separate the handler-mapping order from route assembly

**Status:** finished

**Goal:** Item G from `todo.md` (the other half of `00006` Task 7). `GatewayRouterFunctionConfiguration` has
two responsibilities: it assembles the gateway routes, and it moves Spring MVC's `RouterFunctionMapping` behind
the annotated controllers. The second one moves into its own configuration class, so the route configuration
only assembles routes.

**Branch:** `feature/00007-extension-registries` (never commit to `main`).

## Current state (problem)

`GatewayRouterFunctionConfiguration` contains the `static @Bean` `routerFunctionMappingAfterControllers()`, a
`BeanPostProcessor` that sets `RouterFunctionMapping` to order `1`. Spring MVC registers that mapping at order
`-1`, ahead of `RequestMappingHandlerMapping`, so a configured catch-all route (`/**`) would otherwise proxy
`/auth/**` and `/.well-known/jwks` downstream instead of reaching `LoginController` and `JwksController`.

- The post-processor does not use the class's state (`MainConfig`, route filters). It only shares the file.
- It has no unit test. It is covered only indirectly by integration tests whose configurations contain a `/**`
  route (for example `LoginLogoutTest`, `JwksControllerTest`).
- The docs show the dispatch (`Architecture-Request-Handling.md`, diagram "Path") but don't say that OAG's own
  endpoints take precedence over configured routes.

## Decisions

1. **New class `org.owasp.oag.gateway.HandlerMappingOrderConfiguration`** (`@Configuration`). It stays in
   `gateway`, because it decides how gateway routes rank against OAG's own endpoints. `gateway` is already in
   the component scan of `OAGBeanConfiguration`, so no wiring changes.
2. **The bean method moves unchanged:** same name `routerFunctionMappingAfterControllers`, still `static`, same
   order `1`, and its Javadoc explaining why moves with it. `static` stays, because a `BeanPostProcessor` is
   created before ordinary beans; a static factory method doesn't instantiate its configuration class early.
3. **No constant for the order.** The literal `1` stays, explained by the moved Javadoc. A public constant would
   become API, and a test asserting against it would be circular. The test asserts the ranking instead
   (see step 1).
4. `GatewayRouterFunctionConfiguration` keeps its name and route assembly; its imports of `BeanPostProcessor` and
   `RouterFunctionMapping` go.
5. No behaviour change.

## Review findings incorporated

- Confirmed: Spring MVC 7.0.9 registers `RouterFunctionMapping` at `-1` and `RequestMappingHandlerMapping` at
  `0`; nothing else changes the order. Order `1` lies before `beanNameHandlerMapping` (`2`) and the static
  resource handler (`LOWEST_PRECEDENCE - 1`).
- Confirmed: `LoginLogoutTest` (`localServerConfiguration.yaml`) and `JwksControllerTest`
  (`defaultKeyRotationConfiguration.yaml`) use a `/**` route; many more integration tests do too.
- Confirmed: every test context imports `OWASPApplicationGatewayApplication` (full scan), so the new class is
  found everywhere; no context lists `GatewayRouterFunctionConfiguration` explicitly. No other code refers to
  the bean.
- A new `RouterFunctionMapping` defaults to `LOWEST_PRECEDENCE`, so the test sets `-1` first. The test
  asserts the ranking, not the literal, and the constant was dropped.

## Steps

### 0. Preparation
- [x] Baseline `./gradlew build`: 218 tests, 0 failures.
- [x] Confirm the safety net: with the bean temporarily disabled,
      `./gradlew test --tests '*LoginLogoutTest' --tests '*JwksControllerTest'` fails. Restore immediately and
      check with `git diff` that nothing of the disabled state remains. If neither fails, add an integration
      test first that sends `/auth/...` and `/.well-known/jwks` through a configuration with a `/**` route and
      expects the controllers to answer. (Result: 4 of 7 tests failed; restored, `git diff` empty.)

### 1. Unit test first (red)
- [x] `HandlerMappingOrderConfigurationTest`, calling `postProcessAfterInitialization` directly (the
      "before" method is a no-op default):
  - a `RouterFunctionMapping` set to `-1` first (as Spring MVC does) ends up after the controllers (order > `0`)
    and before the static resources (order < `Ordered.LOWEST_PRECEDENCE - 1`);
  - the same instance is returned;
  - a `RequestMappingHandlerMapping` set to `0` first keeps order `0`.
  Fails to compile until the class exists.

### 2. Move
- [x] Create `HandlerMappingOrderConfiguration` with the moved bean and its Javadoc (class Javadoc too).
- [x] Remove the bean and the unused imports from `GatewayRouterFunctionConfiguration`; its class Javadoc only
      describes route assembly.

### 3. Verify
- [x] The unit test is green; `GatewayRouterFunctionConfigurationTest`, `LoginLogoutTest`, `JwksControllerTest`
      stay green.
- [x] The bean is registered once: only `HandlerMappingOrderConfiguration` defines
      `routerFunctionMappingAfterControllers`, and `GatewayRouterFunctionConfiguration` no longer mentions
      `BeanPostProcessor` (`grep`).
- [x] `./gradlew build` green, including `requireJavadoc`, `PackageCycleTest` and `LayeringTest`: 220 tests, 0 failures.

### 4. Documentation
- [x] `Architecture-Request-Handling.md`: one sentence below the diagram saying that OAG's own endpoints
      (`/auth/**`, `/.well-known/jwks`) take precedence over configured routes, even a catch-all `/**`. No
      class names needed.
- [x] Glossary: no change expected.
- [x] `todo.md`: mark G done.
- [x] Mark this plan finished.

## Risks

- If the bean were lost or registered under a different condition, a catch-all route would shadow OAG's own
  endpoints. Step 0 makes sure the existing integration tests catch exactly that.
- Breaking change only for code that refers to `GatewayRouterFunctionConfiguration.routerFunctionMappingAfterControllers`
  directly. It is not a documented API.
