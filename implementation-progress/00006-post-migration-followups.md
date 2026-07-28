# Post-migration follow-ups Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the follow-ups left open by the final review of the WebFlux → virtual-threads migration (iteration `00005`): bound the session blacklist's unbounded growth, forward form bodies byte-for-byte, prove the `errorReason` log line end to end, and clear the documentation and structural findings.

**Architecture:** Eight independent tasks against the already-migrated servlet/virtual-thread codebase. Two of them are ordered and must not be swapped: `cleanup()` becomes safe to call concurrently (Task 1) **before** anything schedules it (Task 2) — doing it the other way round converts a latent race into a live security bug. The remaining six are independent of each other and of the first two.

**Tech Stack:** Spring Cloud Gateway Server MVC 5.0.2 · Spring Boot 4.1.0 · Spring Cloud 2025.1.2 · Tomcat · Java 25 virtual threads · Gradle 9.6.1

## Global Constraints

- **Baseline is green and must stay green:** `./gradlew build --console=plain` from `oag/` prints `BUILD SUCCESSFUL` with **137 tests / 52 classes / 0 failures / 0 skipped**. Every task ends there. Never `-PskipTests`; never `@Disabled`; never weaken or delete an existing assertion.
- **Do not change versions.** Spring Boot `4.1.0`, Spring Cloud `2025.1.2`, Gradle `9.6.1`, Java `25`. Version stays `0.8.0-SNAPSHOT` — this iteration ships with the same release.
- **Single `spring:` block per YAML file.** `oag/src/main/resources/application.yaml` must have exactly one top-level `spring:` key; SnakeYAML merges duplicates unpredictably. Verify with `grep -c "^spring:" oag/src/main/resources/application.yaml` → `1`, before and after any edit.
- **Javadoc is build-enforced** (`check` depends on `requireJavadoc`): every public/protected/package-private class, method and field needs Javadoc.
- **Documentation states the current state only** — no migration history, no "this used to be X". Project directive in `.claude/CLAUDE.md`. Use mermaid for diagrams.
- **`X_OAG_USER_PREFIX` must not be deleted.** The final review called it dead; it is not — `RequestHeaderUserMapperTest:25` uses it. Only `X_OAG_PROVIDER` is unused.
- **The swagger sidebar link is explicitly out of scope** for this iteration.
- **Every new test must be mutation-checked:** break the thing it guards, confirm it fails, revert, and report the observed failure. A test that passes both ways guards nothing — the `00005` iteration shipped four such assertions before they were caught.
- English in code, config, commit messages and docs. Commit after every task.

**Working directory:** `./gradlew` commands run from `oag/`. Paths below are relative to the repository root.

**Verified reference facts** (checked during review of `00005` — do not re-derive):

- `LocalPersistentBlacklist.cleanup()` currently has exactly two callers: the constructor and the test-only `deleteAllEntries()`. Nothing schedules it, so the blacklist prunes only at restart.
- `invalidateSession` writes the map **before** the bloom filter. That ordering is what makes the Task 1 fix correct; do not reverse it.
- Guava `BloomFilter` is backed by a lock-free `AtomicLongArray`, so concurrent `put`/`mightContain` on one instance are safe. Only the *field reference* publication is not. A bloom filter only ever gains bits, so adding a key twice is harmless.
- SCG's `FormFilter` is gated by `@ConditionalOnProperty(prefix = GatewayMvcProperties.PREFIX, name = "form-filter.enabled", matchIfMissing = true)`.
- Spring's `FormContentFilter` handles exactly `PUT`, `PATCH`, `DELETE`; it is already disabled via `spring.mvc.formcontent.filter.enabled: false`.
- All four `getParameter(...)` callers in `oag/src/main` (`LoginController:171`, `LoginController:373`, `Oauth2Driver:139,143`) are reached only from `@GetMapping` handlers, where the servlet spec sources parameters from the query string alone.
- `DispatcherServlet.initHandlerExceptionResolvers` detects **all** `HandlerExceptionResolver` beans and sorts them; an extra bean is additive, not a replacement.

---

## File Structure

| File | Change | Responsibility afterwards |
| ---- | ------ | ------------------------- |
| `oag/src/main/java/org/owasp/oag/services/blacklist/LocalPersistentBlacklist.java` | Modify (T1) | `cleanup()` safe to call concurrently with `invalidateSession`/`isInvalidated`. |
| `oag/src/main/java/org/owasp/oag/services/blacklist/SessionBlacklist.java` | Modify (T1, T2) | Interface now documents the thread-safety contract and that OAG schedules `cleanup()`. |
| `oag/src/main/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTask.java` | Create (T2) | Schedules `cleanup()` on the existing `cleanupScheduler`. Keeps scheduling out of the blacklist itself. |
| `oag/src/main/java/org/owasp/oag/logging/TraceContextBridge.java` | Modify (T6) | One `TraceContext` per request; the two header-name accessors are gone. |
| `oag/src/main/java/org/owasp/oag/filters/servlet/TraceContextFilter.java` | Modify (T6) | Passes a header-lookup function to the bridge. |
| `oag/src/main/java/org/owasp/oag/gateway/GatewayRouterFunctionConfiguration.java` | Modify (T7) | Route assembly only. |
| `oag/src/main/java/org/owasp/oag/gateway/HandlerMappingOrderConfiguration.java` | Create (T7) | Owns the `RouterFunctionMapping` order override. |
| `oag/src/main/java/org/owasp/oag/cookies/CookieReader.java` | Create (T7) | Reads a named cookie from a request. |
| `oag/src/main/java/org/owasp/oag/filters/servlet/ExtractAuthenticationFilter.java` | Modify (T7) | No longer hosts a general cookie utility. |
| `oag/src/main/java/org/owasp/oag/services/tokenMapping/header/RequestHeaderUserMapper.java` | Modify (T5) | Javadoc matches behaviour; `X_OAG_PROVIDER` removed. |
| `oag/src/test/java/org/owasp/oag/integration/testInfrastructure/TestClientFactory.java` | Modify (T8) | Reuses one client per port instead of leaking one per test method. |
| `www/docs/docs/Custom-(code)-extensions.md` | Modify (T2, T3) | Correct `cleanup()` contract; warn about `getParameter` consuming bodies. |
| `www/docs/docs/Architecture-Request-Handling.md` | Modify (T3, T6) | Documents body passthrough and the response-header ownership boundary. |

**New tests:** `LocalPersistentBlacklistConcurrencyTest` (T1), `SessionBlacklistCleanupTaskTest` (T2), `RequestFailureLoggerIntegrationTest` (T4), a `Secure`-attribute assertion (T8). Task 3 adds no test — its fix was abandoned, see that task.

---

### Task 1: Make `LocalPersistentBlacklist.cleanup()` safe to call concurrently

**This must land before Task 2.** Today `cleanup()` builds a replacement bloom filter from a snapshot and then assigns it. A `invalidateSession` that runs in between writes into the *discarded* filter, so `isInvalidated` later takes the "not in bloom filter" short-circuit and reports a **logged-out session as valid**. Unreachable today only because nothing schedules `cleanup()`.

**Files:**
- Modify: `oag/src/main/java/org/owasp/oag/services/blacklist/LocalPersistentBlacklist.java`
- Modify: `oag/src/main/java/org/owasp/oag/services/blacklist/SessionBlacklist.java` (javadoc only)
- Test: `oag/src/test/java/org/owasp/oag/services/blacklist/LocalPersistentBlacklistConcurrencyTest.java` (new)

**Interfaces:**
- Consumes: `PersistentMap<Integer>`, `GlobalClockSource.getEpochSeconds()` (unchanged).
- Produces: no signature change. `SessionBlacklist`'s three methods keep their signatures; only the documented contract changes.

- [x] **Step 1: Write the failing concurrency test**

Create `oag/src/test/java/org/owasp/oag/services/blacklist/LocalPersistentBlacklistConcurrencyTest.java`. It must invalidate ids from several threads while `cleanup()` runs repeatedly, then assert every id is still reported invalidated:

```java
package org.owasp.oag.services.blacklist;

import org.junit.jupiter.api.Test;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.persistentmap.InMemoryPersistentMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that cleanup() can run concurrently with invalidateSession without losing an entry.
 * <p>
 * cleanup() replaces the bloom filter. If it does so without accounting for entries added while it
 * runs, isInvalidated takes its "not in bloom filter" short-circuit and reports a logged-out session
 * as still valid - so this is a security property, not a tidiness one.
 */
class LocalPersistentBlacklistConcurrencyTest {

    private static final int WRITER_THREADS = 4;
    private static final int IDS_PER_WRITER = 5000;

    @Test
    void cleanupRunningConcurrentlyNeverLosesAnInvalidatedSession() throws Exception {

        var clockSource = new GlobalClockSource();
        var blacklist = new LocalPersistentBlacklist(clockSource, new InMemoryPersistentMap<>());

        var startGate = new CountDownLatch(1);
        var writersDone = new CountDownLatch(WRITER_THREADS);
        var keepCleaning = new AtomicBoolean(true);
        var failures = new ArrayList<String>();
        var writtenIds = new ArrayList<String>();

        // Hammer cleanup() while the writers run
        var cleaner = new Thread(() -> {
            while (keepCleaning.get()) {
                blacklist.cleanup();
            }
        });

        var writers = new ArrayList<Thread>();
        for (int w = 0; w < WRITER_THREADS; w++) {
            final int writer = w;
            writers.add(new Thread(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < IDS_PER_WRITER; i++) {
                        var id = "writer-" + writer + "-id-" + i;
                        // A long ttl, so nothing expires during the test
                        blacklist.invalidateSession(id, 3600);
                        synchronized (writtenIds) {
                            writtenIds.add(id);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    writersDone.countDown();
                }
            }));
        }

        cleaner.start();
        writers.forEach(Thread::start);
        startGate.countDown();

        assertTrue(writersDone.await(30, TimeUnit.SECONDS), "writers did not finish");
        keepCleaning.set(false);
        cleaner.join();

        // Every id written must still be reported as invalidated
        List<String> ids;
        synchronized (writtenIds) {
            ids = List.copyOf(writtenIds);
        }
        for (var id : ids) {
            if (!blacklist.isInvalidated(id)) {
                failures.add(id);
            }
        }

        assertTrue(failures.isEmpty(),
                failures.size() + " of " + ids.size() + " invalidated sessions were reported valid, e.g. "
                        + failures.stream().limit(5).toList());
    }
}
```

- [x] **Step 2: Run it and confirm it fails**

Run: `./gradlew test --tests 'org.owasp.oag.services.blacklist.LocalPersistentBlacklistConcurrencyTest' --console=plain`
Expected: FAIL, with the assertion reporting some number of invalidated sessions read back as valid.

This test is inherently racy, so it may occasionally pass by luck. Run it three times to confirm it fails reliably:
`for i in 1 2 3; do ./gradlew test --tests '...LocalPersistentBlacklistConcurrencyTest' --console=plain --rerun-tasks 2>&1 | grep -E "PASSED|FAILED"; done`
If it never fails, increase `IDS_PER_WRITER` until it does — do not proceed to Step 3 until you have seen it fail, or you cannot know the fix works.

**Observed:** at `IDS_PER_WRITER = 250` the test passed 3/3 — too short a window. Raised to `5000`
(20000 identifiers total), which failed 3/3 with e.g.
`5727 of 20000 invalidated sessions were reported valid, e.g. [writer-2-id-3374, ...]`.

- [x] **Step 3: Make the field `volatile` and repopulate after publishing**

In `LocalPersistentBlacklist.java`, change the field declaration to `volatile` and replace `cleanup()`:

```java
    /**
     * Bloom filter for efficient negative lookups.
     * <p>
     * Volatile because {@link #cleanup()} replaces the instance while request threads read it.
     */
    protected volatile BloomFilter<CharSequence> bloomFilter;
```

```java
    /**
     * Removes expired entries from the blacklist and rebuilds the bloom filter.
     * <p>
     * Safe to run concurrently with {@link #invalidateSession(String, int)} and
     * {@link #isInvalidated(String)}. The rebuild cannot lose a concurrently invalidated identifier:
     * invalidateSession writes the map before the filter, and this method repopulates from the map
     * *after* publishing the new filter. So an identifier whose map write landed before the second
     * pass is picked up by that pass, and one whose map write landed after it is written by its own
     * thread into the already-published new filter. A bloom filter only ever gains bits, so adding an
     * identifier twice is harmless.
     */
    @Override
    public void cleanup() {

        int currentTimeSeconds = clockSource.getEpochSeconds();

        // Drop expired entries. Re-read before removing so an entry refreshed since the snapshot stays.
        blacklist.entrySet().stream()
                .filter(e -> e.getValue() < currentTimeSeconds)
                .forEach(e -> {
                    Integer expiry = blacklist.get(e.getKey());
                    if (expiry != null && expiry < currentTimeSeconds) {
                        blacklist.remove(e.getKey());
                    }
                });

        var newFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),
                blacklist.size() + EXPECTED_INSERTIONS, EXPECTED_BLOOM_FILTER_FALSE_POSITIVES);
        blacklist.entrySet().forEach(e -> newFilter.put(e.getKey()));

        // Publish before the second pass, so a writer that reads the field from here on writes into it
        bloomFilter = newFilter;

        // Second pass: catch identifiers whose map write landed while the filter was being built
        blacklist.entrySet().forEach(e -> newFilter.put(e.getKey()));
    }
```

- [x] **Step 4: Confirm the test now passes, repeatedly**

Run the three-times loop from Step 2 again.
Expected: PASS every time. Then mutation-check the fix: delete the second-pass line, confirm the test fails again, and restore it. Report both observations.

**Observed:** 8/8 runs PASSED after the fix. Mutation check (second-pass line deleted, `volatile`
kept): 7 of 8 runs FAILED, e.g. `7645 of 20000 invalidated sessions were reported valid`. The line
was restored.

- [x] **Step 5: Document the contract on the interface**

In `SessionBlacklist.java`, extend the `cleanup()` Javadoc so implementors know what is required of them:

```java
    /**
     * Should clean up obsolete entries in the blacklist (See expiration).
     * <p>
     * OAG calls this periodically on a background thread, so an implementation must be safe to call
     * concurrently with {@link #invalidateSession(String, int)} and {@link #isInvalidated(String)},
     * and must not lose an identifier invalidated while it runs.
     */
    void cleanup();
```

- [x] **Step 6: Full build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, 138 tests, 0 failures.

**Observed:** `BUILD SUCCESSFUL` — 138 tests / 53 classes / 0 failures / 0 skipped.

```bash
git add oag/src/main/java/org/owasp/oag/services/blacklist oag/src/test/java/org/owasp/oag/services/blacklist/LocalPersistentBlacklistConcurrencyTest.java
git commit -m "fix: make session blacklist cleanup safe to run concurrently"
```

---

### Task 2: Schedule blacklist cleanup at twice the session duration

The blacklist prunes only at restart today, so `session-blacklist.db` and its in-memory map grow with every logout for the lifetime of the process. Schedule `cleanup()` on the **existing** `cleanupScheduler` bean.

**Interval: twice the configured session duration** — `2 × config.getSessionBehaviour().getSessionDuration()` seconds. Derived from existing configuration on purpose; do **not** add a new config key. An entry's TTL never exceeds one session duration, so this bounds an expired entry's residency at roughly two further durations without needing a tuning knob.

**Files:**
- Create: `oag/src/main/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTask.java`
- Modify: `www/docs/docs/Custom-(code)-extensions.md`
- Test: `oag/src/test/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTaskTest.java` (new)

**Interfaces:**
- Consumes: `SessionBlacklist.cleanup()` (Task 1, now concurrency-safe), `MainConfig.getSessionBehaviour().getSessionDuration()`, and the `cleanupScheduler` bean (`ThreadPoolTaskScheduler`, defined in `infrastructure/SchedulerConfiguration`).
- Produces: a `@Component` with no public API beyond its constructor; nothing else depends on it.

- [x] **Step 1: Read how the existing scheduled task is wired**

Run: `sed -n '40,60p;110,125p' oag/src/main/java/org/owasp/oag/services/keymgm/LocalRsaJwkStore.java`
Expected: you can see the `@Qualifier("cleanupScheduler") ThreadPoolTaskScheduler` injection and its self-rescheduling one-shot `scheduler.schedule(...)` pattern.

**Deliberately do not copy that pattern.** Use `scheduleAtFixedRate`, which cannot fail to re-arm — the one-shot pattern loses the schedule permanently if the re-arm throws. Confirm the method exists with:
`javap -cp $(find ~/.gradle/caches -name 'spring-context-*.jar' | head -1) org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler | grep -i scheduleAtFixedRate`

- [x] **Step 2: Create the task**

Create `oag/src/main/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTask.java`:

```java
package org.owasp.oag.services.blacklist;

import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Periodically prunes expired entries from the session blacklist.
 * <p>
 * Without this the blacklist only prunes at startup, so both the persisted file and the in-memory map
 * grow with every logout for the lifetime of the process.
 */
@Component
public class SessionBlacklistCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SessionBlacklistCleanupTask.class);

    private final SessionBlacklist sessionBlacklist;

    /**
     * Schedules the periodic cleanup.
     *
     * @param sessionBlacklist The blacklist to prune
     * @param config           Main configuration, read for the session duration
     * @param scheduler        The shared cleanup scheduler
     */
    @Autowired
    public SessionBlacklistCleanupTask(SessionBlacklist sessionBlacklist, MainConfig config,
                                       @Qualifier("cleanupScheduler") ThreadPoolTaskScheduler scheduler) {

        this.sessionBlacklist = sessionBlacklist;

        // An entry's ttl never exceeds one session duration, so running at twice that bounds how long
        // an expired entry lingers without introducing a separate tuning knob.
        var interval = Duration.ofSeconds(2L * config.getSessionBehaviour().getSessionDuration());

        scheduler.scheduleAtFixedRate(this::cleanup, interval);
        log.info("Scheduled session blacklist cleanup every {} seconds", interval.toSeconds());
    }

    /**
     * Runs one cleanup pass, swallowing any failure so the fixed-rate schedule is not cancelled.
     */
    void cleanup() {

        try {
            sessionBlacklist.cleanup();
            log.debug("Session blacklist cleanup completed");
        } catch (Exception e) {
            log.error("Session blacklist cleanup failed. Expired entries will accumulate until this succeeds.", e);
        }
    }
}
```

The `try/catch` is load-bearing: `ScheduledExecutorService` cancels a fixed-rate task permanently if it throws, which would silently restore the unbounded growth this task exists to prevent.

- [x] **Step 3: Write the test**

Create `oag/src/test/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTaskTest.java`. Assert two things: the interval is twice the session duration, and a throwing `cleanup()` does not propagate. Build a real `ThreadPoolTaskScheduler` and a stub `SessionBlacklist` that counts calls; for the interval, either capture it with a `ThreadPoolTaskScheduler` subclass overriding `scheduleAtFixedRate(Runnable, Duration)`, or set a small `sessionDuration` in the `MainConfig` fixture and assert the stub is called more than once within a bounded wait. Prefer capturing the interval — it asserts the requirement directly rather than by timing.

Construct `MainConfig` the way `TraceContextBridgeTest` does (load a config through `TestFileConfigLoader`, or set the field with `ReflectionTestUtils`) — read that test first and reuse its approach rather than inventing one.

- [x] **Step 4: Run the test**

Run: `./gradlew test --tests 'org.owasp.oag.services.blacklist.SessionBlacklistCleanupTaskTest' --console=plain`
Expected: PASS. Mutation-check both cases: change `2L *` to `1L *` and confirm the interval assertion fails; remove the `try/catch` and confirm the swallowing assertion fails. Revert both, report both observations.

- [x] **Step 5: Correct the documentation**

`www/docs/docs/Custom-(code)-extensions.md` currently says `cleanup` "may be called regularly by the runtime". That was aspirational; now it is true, and it carries a requirement. Change it to state that OAG schedules `cleanup()` every two session durations on a background thread, and that an implementation must therefore be safe to call concurrently with the other two methods and must not lose an identifier invalidated while it runs.

- [x] **Step 6: Confirm it actually runs in a real context**

Run: `./gradlew test --tests 'org.owasp.oag.integration.DefaultConfigurationTest' --console=plain -i 2>&1 | grep -i "Scheduled session blacklist cleanup"`
Expected: the startup line appears, proving the bean is constructed in a real application context. If it does not, the component is not being scanned — check `OAGBeanConfiguration`'s explicit `@ComponentScan` list includes `org.owasp.oag.services`.

- [x] **Step 7: Full build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, 139+ tests, 0 failures.

```bash
git add oag/src/main/java/org/owasp/oag/services/blacklist oag/src/test/java/org/owasp/oag/services/blacklist/SessionBlacklistCleanupTaskTest.java "www/docs/docs/Custom-(code)-extensions.md"
git commit -m "feat: prune the session blacklist periodically instead of only at startup"
```

---

### Task 3: Document how form bodies are handled

**The original plan for this task was wrong and its fix was abandoned.** It proposed disabling Spring
Cloud Gateway's `FormFilter` to get byte-for-byte `POST` form-body passthrough. Two facts, both verified
against the 5.0.2 sources, invalidate that:

1. `FormFilter` is load-bearing, not cosmetic. Its `FormContentRequestWrapper` masks `getParameter` and
   `getParameterMap` down to **query-string parameters only**, and `ProxyExchangeHandlerFunction:92` calls
   `serverRequest.params()` to build the downstream URI. Disabling the filter therefore makes body
   parameters **leak into the downstream query string** and loses the body entirely — a `POST /foo` with
   body `a=1&b=2` is forwarded as `POST /foo?a=1&b=2` with no body. Strictly worse than the problem.
2. The property key is `spring.cloud.gateway.server.webmvc.form-filter.enabled`;
   `GatewayMvcProperties.PREFIX` is `spring.cloud.gateway.server.webmvc`.

The real defect is also much narrower than the plan claimed. Parameter order and duplicate keys **are**
preserved. The only difference is the encoding of a space: `%20` sent, `+` received. Both decode to a
space under `application/x-www-form-urlencoded`, so a downstream system parsing the form sees identical
parameters. It matters only to a downstream computing a signature over the raw body bytes.

A working fix does exist — a Tomcat-specific `TomcatConnectorCustomizer` setting `parseBodyMethods("")`
**plus** the config property, where either half alone is a regression — and it was rejected: two coupled
settings guarding a semantically neutral byte difference, tied to one servlet container, and globally
disabling form parsing so `getParameter` on a `POST` silently returns nothing.

**This task is therefore documentation only.**

**Files:**
- Modify: `www/docs/docs/Architecture-Request-Handling.md`
- Modify: `www/docs/docs/Custom-(code)-extensions.md`

**Interfaces:** none.

- [x] **Step 1: Document the body-forwarding behaviour**

Added a "Request bodies" section to `Architecture-Request-Handling.md` stating that bodies are forwarded
unbuffered except for a form-content `POST`, which is parsed and re-encoded; that order and repeated names
survive while `%20` becomes `+`; that both decode to a space so parsed parameters are unaffected; and that
`PUT`/`PATCH`/`DELETE` form bodies pass through byte-for-byte.

- [x] **Step 2: Warn extension authors about consuming the body**

Added a "Reading the request body in an extension" section to `Custom-(code)-extensions.md`: do not call
`getParameter`/`getParameterMap` on a request with a body outside the form-`POST` case, because it consumes
the body with nothing to rebuild it and the downstream system silently receives an empty one. Points at
`CsrfDoubleSubmitCookieValidation` for the query-string pattern and at `MvcUtils.getOrCacheBody` plus
`BeforeFilterFunctions.adaptCachedBody()` for the case where the body really is needed, with
`CsrfValidationFilterWithBody` as the worked example.

- [x] **Step 3: Verify**

`./gradlew build` → `BUILD SUCCESSFUL`, 142 tests / 54 classes / 0 failures (unchanged — no code changed).
`npm run docs:build` in `www/` → `build complete`.


### Task 4: Prove the `errorReason` log line end to end

`RequestFailureLogger` is only unit-tested by calling `resolveException` directly. Nothing asserts it is registered as a `HandlerExceptionResolver` bean and actually reached — and preserving that log detail is an explicit requirement, so it is the one property with no coverage of its own wiring.

**Files:**
- Test: `oag/src/test/java/org/owasp/oag/filters/servlet/RequestFailureLoggerIntegrationTest.java` (new)

**Interfaces:** none — test only. Do not change production code in this task.

- [ ] **Step 1: Write the test**

Create an integration test that boots a real context, attaches a Logback `ListAppender` to `RequestFailureLogger`'s logger, drives a request that produces a `ResponseStatusException` with a reason, and asserts the captured output contains it.

The cheapest reliable trigger is an invalid `returnUrl`, which `LoginController.loadLoginReturnUrl` rejects with `ResponseStatusException(BAD_REQUEST, "Invalid return url")`:

```java
        var result = webClient.get().uri("/auth/local/login?returnUrl=http://evil.example.com")
                .exchange()
                .expectStatus().isBadRequest();
```

Then assert some captured event's formatted message contains `errorReason: 'Invalid return url'`.

Extend `WiremockTest` so the login route and provider exist, and follow the `@SpringBootTest` annotation shape its other subclasses use. Attach the appender in `@BeforeEach` and detach it in `@AfterEach` so it cannot leak into other tests:

```java
        var logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestFailureLogger.class);
        appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
```

Note the log level: `RequestFailureLogger` logs at info via `LoggingUtils.logInfo`, and these test classes already set `logging.level.org.owasp.oag=TRACE`, so info is enabled. Assert on `event.getFormattedMessage()`, not `getMessage()` — the latter returns the unsubstituted `{}` template.

- [ ] **Step 2: Run it**

Run: `./gradlew test --tests 'org.owasp.oag.filters.servlet.RequestFailureLoggerIntegrationTest' --console=plain`
Expected: PASS.

- [ ] **Step 3: Mutation-check that it proves the wiring**

Temporarily remove `@Component` from `RequestFailureLogger` and re-run. Expected: FAIL — no captured event. Restore the annotation and report the observed failure.

This is the whole point of the task: the existing unit test passes with or without `@Component`, so only this check demonstrates the bean registration.

- [ ] **Step 4: Full build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, one more test than before, 0 failures.

```bash
git add oag/src/test/java/org/owasp/oag/filters/servlet/RequestFailureLoggerIntegrationTest.java
git commit -m "test: prove the failure reason is logged through a running context"
```

---

### Task 5: Correct `RequestHeaderUserMapper`'s documentation and public surface

Its class Javadoc claims "the username is added as `X-OAG-USER: foobar`", and it declares a public `X_OAG_PROVIDER` constant. The implementation emits only the configured `mappings`. The documentation is aspirational and was never implemented; the decision is to make the documentation truthful rather than to build the behaviour.

**Files:**
- Modify: `oag/src/main/java/org/owasp/oag/services/tokenMapping/header/RequestHeaderUserMapper.java`

**Interfaces:**
- Produces: `X_OAG_PROVIDER` is **removed** (a public constant, so an API break — acceptable, `0.8.0-SNAPSHOT` is unreleased). `X_OAG_USER_PREFIX` **stays**; `RequestHeaderUserMapperTest:25` uses it.

- [x] **Step 1: Confirm which constant is actually unused**

Run: `grep -rn "X_OAG_PROVIDER\|X_OAG_USER_PREFIX" oag/src www`
Expected: `X_OAG_PROVIDER` appears only at its declaration; `X_OAG_USER_PREFIX` appears at its declaration **and** in `RequestHeaderUserMapperTest`. If that is not what you see, stop and report — the plan's premise is wrong.

- [x] **Step 2: Rewrite the class Javadoc to describe actual behaviour**

Replace the class Javadoc so it says what the class does: it adds the headers named in the security profile's `mappings`, each value rendered through the user-mapping templating engine, and adds nothing when there is no session. Keep the existing security warning — that this is weaker than the JWT mapping because a caller could forge the headers if OAG can be bypassed — since that is still true and still worth saying. Do not describe `X-OAG-USER` or `X-OAG-Provider` as things it emits.

- [x] **Step 3: Delete `X_OAG_PROVIDER`**

Remove the constant and its Javadoc. Leave `X_OAG_USER_PREFIX` untouched.

- [x] **Step 4: Build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, test count unchanged, 0 failures. A failure here means something used the constant after all — go back to Step 1.

```bash
git add oag/src/main/java/org/owasp/oag/services/tokenMapping/header/RequestHeaderUserMapper.java
git commit -m "docs: describe what the request header user mapper actually emits"
```

---

**Outcome:** Step 1's re-verification confirmed the plan and contradicted the final review: `X_OAG_PROVIDER` appeared only at its own declaration and was deleted; `X_OAG_USER_PREFIX` is used by `RequestHeaderUserMapperTest:25` and was kept, with its Javadoc corrected to say it is a convention offered for the `mappings` configuration rather than something the mapper applies. The class Javadoc now describes what the mapper does - it emits exactly the configured `mappings`, rendered through the templating engine, and forwards unchanged when there is no session - and carries a real configuration example taken from `oag/src/test/resources/headerUserMappingConfig.yaml`, which uses `X-USER-PROVIDER`/`X-USER-ID` and so demonstrates that neither constant reflected actual behaviour. The existing security warning was kept, with `OAg` corrected to `OAG`. Suite unchanged at 142/54/0.

### Task 6: Build one `TraceContext` per request, and document the response-header boundary

`TraceContextFilter` calls `traceContext.getMainRequestHeader()`, `getSecondaryRequestHeader()` and `createTraceHeaders(...)`, and each of those constructs a fresh `TraceContext` — three per request, where the WebFlux code built one. Collapse it by letting the bridge look the headers up itself through a caller-supplied function.

**Files:**
- Modify: `oag/src/main/java/org/owasp/oag/logging/TraceContextBridge.java`
- Modify: `oag/src/main/java/org/owasp/oag/filters/servlet/TraceContextFilter.java`
- Modify: `oag/src/test/java/org/owasp/oag/logging/TraceContextBridgeTest.java`
- Modify: `www/docs/docs/Architecture-Request-Handling.md`

**Interfaces:**
- Produces: `TraceContextBridge.createTraceHeaders(UnaryOperator<String> headerValueLookup)` returning `TraceHeaders`. The two-argument `createTraceHeaders(String, String)` and both header-name accessors are **removed**.

- [ ] **Step 1: Change the bridge to one instantiation**

Replace `createTraceHeaders(String, String)` and delete `getMainRequestHeader()` / `getSecondaryRequestHeader()`:

```java
    /**
     * Decides the tracing headers for one request.
     * <p>
     * Takes a lookup function rather than two header values because only the trace context knows which
     * header names to read, and it must be created exactly once per request.
     *
     * @param headerValueLookup returns the request's value for a header name, or null if absent. Must
     *                          tolerate a null name - a trace profile that forwards nothing has none.
     * @return the trace id plus the headers to add downstream and to the response
     */
    public TraceHeaders createTraceHeaders(UnaryOperator<String> headerValueLookup) {

        final TraceContext requestContext = traceContextFactory.createContextForRequest();

        if (requestContext.forwardIncomingTrace()) {

            var incomingPrimary = headerValueLookup.apply(requestContext.getMainRequestHeader());
            var incomingSecondary = requestContext.acceptAdditionalTraceInfo()
                    ? headerValueLookup.apply(requestContext.getSecondaryRequestHeader())
                    : null;

            if (incomingPrimary == null || incomingPrimary.isEmpty()) {
                // No inbound trace to adopt - the ordinary case for a first-hop request. It must not go
                // through TraceException, or every such request logs. Note the header accessor yields
                // "" rather than null for a header sent with an empty value.
                requestContext.generateNewTraceId();
            } else {
                try {
                    requestContext.applyExistingTrace(incomingPrimary, incomingSecondary);
                } catch (TraceException e) {
                    requestContext.generateNewTraceId();
                }
            }
        } else {
            requestContext.generateNewTraceId();
        }

        // ... the downstreamHeaders / responseHeaders assembly and the return stay exactly as they are
    }
```

Add `import java.util.function.UnaryOperator;`. Keep the rest of the method body — including the non-null guard on the secondary trace info — byte-for-byte.

- [ ] **Step 2: Update the filter**

In `TraceContextFilter.doFilterInternal`, replace the three calls with one, keeping the null-name guard that `NoTraceContext` requires:

```java
        var traceHeaders = traceContext.createTraceHeaders(
                name -> name == null ? null : request.getHeader(name));
```

- [ ] **Step 3: Update the test's call shape only**

`TraceContextBridgeTest` drives `createTraceHeaders(primary, secondary)`. Change those calls to pass a lookup function that returns the same values for the same header names, e.g. a small helper that maps `"traceparent"` → primary and `"tracestate"` → secondary. **Keep every assertion**, especially the absent-header and empty-header cases asserting `applyExistingTraceCalled == false` — they pin the guard that keeps a warning off every first-hop request.

- [ ] **Step 4: Verify, including the guard**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, test count unchanged, 0 failures.

Then confirm the guard survived:
`./gradlew test --tests 'org.owasp.oag.integration.mockserver.tracing.*' --console=plain -i --rerun-tasks 2>&1 | grep -c "traceparent not provided."`
Expected: `0`. Any other number means the guard was lost while restructuring.

Also confirm exactly one context per request by adding a temporary counter to `DefaultTraceContextFactory.createContextForRequest()`, running one tracing test, checking it reports 1 per request, then removing the counter. Report the observed number.

- [ ] **Step 5: Document the response-header ownership boundary**

Add to `www/docs/docs/Architecture-Request-Handling.md`: response headers written by the servlet-tier filters — the trace response header is the one case today — are written straight to the servlet response and are therefore **not** visible to the route-tier `ResponseHeaderFilter`. A security profile's `responseHeaders` section can add, override or `<<remove>>` headers coming from the downstream system, but cannot remove one that a servlet filter wrote. State it as current behaviour, without describing how it used to work.

- [ ] **Step 6: Commit**

```bash
git add oag/src/main/java/org/owasp/oag/logging/TraceContextBridge.java oag/src/main/java/org/owasp/oag/filters/servlet/TraceContextFilter.java oag/src/test/java/org/owasp/oag/logging/TraceContextBridgeTest.java www/docs/docs/Architecture-Request-Handling.md
git commit -m "refactor: build one trace context per request"
```

---

### Task 7: Give `GatewayRouterFunctionConfiguration` one responsibility, and move `findCookie`

Two structural findings, both about things sitting in the wrong place. `GatewayRouterFunctionConfiguration` both assembles routes and reorders a framework bean; `ExtractAuthenticationFilter` hosts a general cookie-reading utility that two other packages now reach into it for.

**Files:**
- Modify: `oag/src/main/java/org/owasp/oag/gateway/GatewayRouterFunctionConfiguration.java`
- Create: `oag/src/main/java/org/owasp/oag/gateway/HandlerMappingOrderConfiguration.java`
- Create: `oag/src/main/java/org/owasp/oag/cookies/CookieReader.java`
- Modify: `oag/src/main/java/org/owasp/oag/filters/servlet/ExtractAuthenticationFilter.java`
- Modify: `oag/src/main/java/org/owasp/oag/services/csrf/CsrfSameSiteStrictCookieValidation.java`
- Modify: `oag/src/main/java/org/owasp/oag/controllers/LoginController.java`

**Interfaces:**
- Produces: `CookieReader.findCookie(HttpServletRequest request, String name)` returning `Optional<Cookie>` (or whatever the current `findCookie` returns — match it exactly). `ExtractAuthenticationFilter.findCookie` is removed.

- [ ] **Step 1: Move the handler-mapping order override into its own configuration**

Cut the `static @Bean` `BeanPostProcessor` out of `GatewayRouterFunctionConfiguration` into a new `HandlerMappingOrderConfiguration`, carrying its explanatory comment with it verbatim — that comment records why order `1` is required and must not be lost. Keep the bean `static`, so it does not force early instantiation of a `MainConfig`-dependent configuration class.

Run: `./gradlew compileJava --console=plain` then the routing test:
`./gradlew test --tests 'org.owasp.oag.gateway.GatewayRouterFunctionConfigurationTest' --console=plain`

- [ ] **Step 2: Verify the precedence still holds where it actually matters**

Run: `./gradlew test --tests 'org.owasp.oag.integration.mockserver.LoginLogoutTest' --tests 'org.owasp.oag.controller.JwksControllerTest' --console=plain`
Expected: PASS. These fail loudly if the order override was lost, because every test config has a catch-all `/**` route that would otherwise shadow `/auth/**` and `/.well-known/jwks`.

- [ ] **Step 3: Extract `findCookie` into the cookies package**

Read the current `ExtractAuthenticationFilter.findCookie` and move it verbatim into a new final utility class `org.owasp.oag.cookies.CookieReader` with a private constructor, matching `CookieWriter`'s shape. Update the three call sites. Keep the method's null-handling exactly as it is — `request.getCookies()` returns null when no cookies were sent, and that is already handled.

- [ ] **Step 4: Confirm nothing still reaches into the filter for it**

Run: `grep -rn "ExtractAuthenticationFilter.findCookie\|findCookie" oag/src`
Expected: hits only in `CookieReader` and its three callers.

- [ ] **Step 5: Full build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, test count unchanged, 0 failures.

```bash
git add oag/src/main/java/org/owasp/oag
git commit -m "refactor: separate handler mapping order from route assembly, move cookie reading to the cookies package"
```

---

### Task 8: Reuse the test HTTP client, and assert the `Secure` cookie attribute

Two test-quality gaps. `TestClientFactory` builds a fresh `SSLContext` and `java.net.http.HttpClient` per test **method** and never closes them — roughly 137 per run, each owning a selector thread, unwinding only on GC. And no test asserts the `Secure` cookie attribute, because every test fixture uses an `http://` `hostUri`, which makes `config.isHttpsHost()` false and `Secure` legitimately absent.

**Files:**
- Modify: `oag/src/test/java/org/owasp/oag/integration/testInfrastructure/TestClientFactory.java`
- Create: `oag/src/test/resources/httpsHostConfiguration.yaml`
- Test: `oag/src/test/java/org/owasp/oag/integration/mockserver/SecureCookieTest.java` (new)

**Interfaces:**
- Consumes: `TestClientFactory.createClient(int port)` and `createClient(int port, Duration timeout)` — keep both signatures; `AbstractServerTest` and `DefaultConfigurationTest` call them.
- Produces: same signatures, now returning a cached client per (port, timeout).

- [ ] **Step 1: Cache the client**

In `TestClientFactory`, hold a `private static final Map<String, RestTestClient>` keyed by port and timeout, and return an existing entry when present. A `ConcurrentHashMap` with `computeIfAbsent` is enough. All tests in a class share one server port, so this collapses to roughly one client per test class.

Do **not** try to close them: there is no lifecycle hook that outlives the whole suite, and a cached client that stays open for the run is the point. Add a comment saying so, so nobody "fixes" it into a per-test client again.

- [ ] **Step 2: Confirm nothing broke**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, 0 failures, test count unchanged. Watch for cross-test interference — a shared client carries no per-request state, but a shared *cookie store* would; confirm `HttpClient.newBuilder()` in this factory does not set a `CookieHandler` (it does not today, and it must not start).

- [ ] **Step 3: Add an https test fixture**

Create `oag/src/test/resources/httpsHostConfiguration.yaml` by copying `localServerConfiguration.yaml` and changing only `hostUri` to `https://localhost:8080`. Everything else stays identical.

Note the server already serves TLS in tests; it is the *configured* `hostUri` that drives `isHttpsHost()`, and therefore the `Secure` and `SameSite=None` cookie attributes.

- [ ] **Step 4: Assert `Secure` on both cookies**

Create `SecureCookieTest` extending `WiremockTest` with a `@TestConfiguration` supplying a `TestFileConfigLoader("/httpsHostConfiguration.yaml")` — copy the pattern from `W3CTracingTest`, which does exactly this for a different config file. Log in with `makeLogin()`, then assert on the **raw `Set-Cookie` headers** that both the login cookie and the CSRF cookie contain `Secure`.

Use the raw header, not a parsed cookie object, for the same reason `LoginLogoutTest` does: attribute-level assertions are the only thing that catches a regression to a cookie API that cannot express them.

- [ ] **Step 5: Mutation-check it**

Temporarily change `CookieConverter.convertLoginCookie` to `.secure(false)`, confirm the test fails, and revert. Report the observed failure. Then also confirm the pre-existing `LoginLogoutTest` `SameSite`/`HttpOnly` assertions still pass — they run against the `http` fixture and must be unaffected.

- [ ] **Step 6: Full build and commit**

Run: `./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, one more test than before, 0 failures.

```bash
git add oag/src/test
git commit -m "test: reuse the test http client and assert the Secure cookie attribute"
```

---

## Definition of Done

- [ ] Every task above is checked off and this plan is marked finished.
- [ ] `./gradlew build` prints `BUILD SUCCESSFUL`; test count has grown by at least five; nothing disabled or weakened.
- [ ] Every new test has been mutation-checked, with the observed failure recorded.
- [ ] `grep -c "^spring:" oag/src/main/resources/application.yaml` → `1`.
- [ ] The session blacklist prunes on a schedule, and `cleanup()` is safe to call while requests are being served.
- [ ] Form bodies reach downstream systems byte-for-byte, for every HTTP method.
- [ ] `npm run docs:build` succeeds in `www/`.

## Deliberately out of scope

- The swagger sidebar link (`www/docs/.vitepress/config.js`, pinned to `OAG/0.4`) — excluded by decision.
- Implementing the `X-OAG-USER` / `X-OAG-Provider` behaviour that `RequestHeaderUserMapper`'s Javadoc described; Task 5 corrects the documentation instead.
- Unifying response-header ownership so a security profile could remove a servlet-filter-written header. Task 6 documents the boundary; no shipped profile needs it changed.
- `MainConfig.hostUrl()`'s non-volatile lazy write, and `AbstractException`'s unreachable `default` branch. Both benign and pre-existing.
- `VirtualThreadProbeFilter`'s static cross-test state, which only matters if the suite is ever run in parallel.
