# 00001 — Replace MapDB with an own persistent map

## Goal

Remove the unmaintained `org.mapdb:mapdb:3.1.0` dependency and its risky transitive
libraries (`org.mapdb:elsa:3.0.0-M5` Java-serialization, `net.jpountz.lz4:lz4:1.3.0`)
by implementing a small, self-contained persistent map in a new package
`org.owasp.oag.persistentmap`. The session blacklist is the only consumer of MapDB.

## Context

MapDB is used in exactly two places, both behind the `SessionBlacklist` interface:

- `services/blacklist/LocalPersistentBlacklist` — a file-backed `HTreeMap<String, Integer>`
  mapping *session identifier → expiry (epoch seconds)*, fronted by a Guava `BloomFilter`
  for O(1) negative lookups.
- `integration/testInfrastructure/LocalInMemoryBlacklist` — the same class with an
  in-memory MapDB (`DBMaker.memoryDB()`).

Operations actually used on the map: `put`, `remove`, `getOrDefault`, iterate entries,
`size`, `clear`, `commit`, `close`. The bloom filter is independent of MapDB and stays.

So the required abstraction is: *a persistent, thread-safe `String → V` map with
iteration, plus an in-memory-only variant for tests.*

## Design

### New package `org.owasp.oag.persistentmap`

```
persistentmap/
├── PersistentMap.java          interface (extends Closeable)
├── InMemoryPersistentMap.java  ConcurrentHashMap-backed, no persistence
└── FilePersistentMap.java      append-only log + internal auto-compaction (composition)
```

**`PersistentMap<V>` (interface, extends `Closeable`)** — pure map contract, no persistence
verbs leak into it:

```java
V get(String key);                 // null if absent
void put(String key, V value);     // durably persisted before returning (file impl); value must be non-null
void remove(String key);
boolean containsKey(String key);
int size();
void clear();
Set<Map.Entry<String, V>> entrySet();   // snapshot copy, safe to iterate
// close() from Closeable — flush + release the file
```
Compaction is deliberately **not** on the interface — it is an internal concern of the file
implementation (see below), so `InMemoryPersistentMap` has no meaningless no-op to carry.

Null values are forbidden: `put(k, null)` throws `NullPointerException`. This keeps the
tombstone encoding (`v == null` on disk) unambiguous.

**`InMemoryPersistentMap<V>`**
- Backed by `ConcurrentHashMap<String, V>`.
- `close()` is a no-op; `entrySet()` returns a defensive copy.
- Used directly as the injected map in tests.

**`FilePersistentMap<V>`** — implements `PersistentMap<V>` by **composition** (holds a private
`ConcurrentHashMap<String,V>` + a log writer), not by extending `InMemoryPersistentMap`. This
keeps the "replay must not re-log" and lock-boundary rules explicit and avoids calling
overridable methods from the constructor.
- Append-only operation log; one JSON record per line (via the already-present Jackson).
  Record shape: `{"k":"<key>","v":<value-or-null>}` where `v == null` is a tombstone
  (delete). Jackson handles key/value escaping, so arbitrary identifiers are newline-safe.
- Value (de)serialization uses Jackson with a `Class<V>` (here `Integer.class`) passed to
  the constructor. **No Java serialization** anywhere — that is the whole point.
- Writes: append the record and `flush()` the writer before returning from `put`/`remove`
  (durability equivalent to MapDB's per-op `commit`).
- Load on construction: replay the log **directly into the internal map** (never via the
  logging `put`/`remove`, so a restart does not re-append every entry). A missing file →
  empty map (not an error). Reads use explicit UTF-8. Any line that fails to parse (a torn
  write, or foreign/binary content) is skipped with a warning rather than failing startup;
  if **any** line was unparseable, compact once after load so the file is rewritten clean
  and the foreign content discarded (see migration note).
- **Auto-compaction (internal):** track appends since the last compaction; when
  `appendsSinceCompaction > liveSize * FACTOR + MIN_APPENDS` (e.g. FACTOR 2, MIN_APPENDS 1000),
  compact inside the write path. This bounds file size regardless of whether any consumer
  ever triggers cleanup — the append-only log cannot grow without bound.
- **Compaction procedure** (under the write lock): flush and **close** the current writer;
  write a temp file **in the same directory**; `flush`+`fsync` the temp file (and fsync the
  parent dir); `Files.move` onto the target (`ATOMIC_MOVE`, falling back to `REPLACE_EXISTING`
  if the platform/FS rejects atomic move); then **reopen** the append writer on the new file.
  On any failure, delete the temp file and keep the existing writer/file. Closing then
  reopening the writer is required: keeping the old handle open would (POSIX) append to the
  now-unlinked inode and silently lose writes, or (Windows) make the move fail with
  `AccessDeniedException`.

### Thread-safety & durability
- Reads (`get`/`containsKey`/`size`/`entrySet`) go straight to the `ConcurrentHashMap`.
- All writes and the whole compaction close→move→reopen sequence hold one `ReentrantLock`,
  so map mutations, log append order, and writer swaps stay consistent; a concurrent read
  never sees a half-swapped writer.
- Per-append durability: `flush()` application buffers (not per-append `fsync`) — a lost last
  append is bounded by the token TTL and the bloom filter tolerates approximation.
- Compaction durability is stronger because it rewrites the whole file: the temp file is
  `fsync`ed before the atomic move, so a crash cannot leave a truncated `session-blacklist.db`
  that loses every live entry at once.

### Blacklist refactor (`LocalPersistentBlacklist`) — constructor injection
- Replace fields `DB db` / `HTreeMap<String,Integer> blacklist` with an injected
  `PersistentMap<Integer> blacklist`.
- New constructor: `LocalPersistentBlacklist(GlobalClockSource clockSource,
  PersistentMap<Integer> blacklist)`. The old `(clockSource, String filename)` constructor
  and the `initDb(...)` seam are removed — the map is now built by the caller and injected.
- Remove all `db.commit()` calls (persistence is now per-write).
- `cleanupBlocking()`: iterate `entrySet()`, `remove` expired entries, then rebuild the bloom
  filter from the post-removal snapshot (unchanged from current behaviour at
  `LocalPersistentBlacklist:106-113`). No explicit compaction call — the removes append
  tombstones and `FilePersistentMap` auto-compacts internally once the threshold is hit.
- `close()` delegates to `blacklist.close()`.
- Replace `import kotlin.text.Charsets` / `Charsets.UTF_8` with
  `java.nio.charset.StandardCharsets.UTF_8` (kotlin-stdlib only arrived transitively via
  MapDB and disappears once MapDB is removed).

### Bean wiring (`OAGBeanConfiguration`)
- `sessionBlacklist(...)` builds and injects the file-backed map:
  `new LocalPersistentBlacklist(clockSource, new FilePersistentMap<>(filename, Integer.class))`.
- The bean already declares `@Bean(destroyMethod = "close")`; blacklist `close()` closes the
  injected map, so the file is released on shutdown.

### Test infrastructure
- `LocalInMemoryBlacklist` is **removed** — constructor injection makes the subclass
  unnecessary. `IntegrationTestConfig` constructs
  `new LocalPersistentBlacklist(clockSource, new InMemoryPersistentMap<>())`.
- `LocalPersistentBlacklistTest` constructs
  `new LocalPersistentBlacklist(clockSource, new FilePersistentMap<>(testDbName, Integer.class))`.

### build.gradle
- Remove `implementation 'org.mapdb:mapdb:3.1.0'`.
- Confirm no remaining `org.mapdb` / `kotlin` imports compile-time.
- Seven source files used `org.jetbrains.annotations.NotNull`, which previously arrived
  transitively via MapDB's `kotlin-stdlib`. Rather than add a new third-party annotations
  library, replace those with JSpecify `org.jspecify.annotations.NonNull` — the null-safety
  standard Spring Framework 7 / Spring Boot 4 already uses (`org.jspecify:jspecify:1.0.0` is
  on the compile classpath via `spring-core`). Declare it explicitly with
  `implementation 'org.jspecify:jspecify'` (version managed by the Spring Boot BOM) since we
  import it directly. Note: `@NonNull` is a `TYPE_USE` annotation, so for a nested return type
  it goes on the simple name (`ServerHttpRequest.@NonNull Builder`), not in modifier position.
- Verified via `./gradlew dependencies`: `org.mapdb:*`, `org.mapdb:elsa`, `net.jpountz.lz4`,
  `eclipse-collections`, `kotlin-*`, and `org.jetbrains:*` are all off the runtime classpath.

### Config / migration note
- The default filename stays `oag.session-blacklist-file: "session-blacklist.db"`
  (`application.yaml` unchanged). On first startup after the change an existing file is an
  old MapDB binary the new loader cannot parse: load reads it with explicit UTF-8 (malformed
  bytes are decoded to replacement chars, never throwing), skips every unparseable line
  (starting empty) and, because unparseable content was seen, immediately compacts —
  rewriting the same `session-blacklist.db` in the clean JSON-line format. Migration is
  transparent; no manual deletion and no operator action required. Starting empty is
  acceptable because entries are short-lived (bounded by token TTL) and the blacklist is a
  best-effort security cache fronted by the bloom filter.
- The old file is a few MB; reading it once line-by-line at startup is a bounded one-off cost.
  A merely torn last line (normal crash) also triggers one compaction on next start — harmless.

## TDD checklist

Per the CLAUDE.md directive, any test touching the filesystem is an **integration test**, so all
`FilePersistentMap` tests live under the `integration` package and use JUnit 5 `@TempDir` (no
fixed cwd filenames, no manual `File.delete()` — the current `LocalPersistentBlacklistTest`
pattern of a fixed working-dir file must not be copied).

Unit tests — `InMemoryPersistentMap` (pure, no filesystem):
- [x] put/get/remove/containsKey/size/clear/entrySet snapshot semantics.
- [x] `put(k, null)` throws `NullPointerException` (shared contract with the file impl).

Integration tests — `FilePersistentMap` (`@TempDir`):
- [x] put then get returns value; remove then get returns null.
- [x] reopening the same file restores all live entries (persistence round-trip).
- [x] a removed key is still absent after reopen (tombstone honoured).
- [x] **append after compaction persists**: put A, force compaction, put B, close, reopen →
      both A and B present (guards the writer close→move→reopen path).
- [x] **auto-compaction threshold**: after many writes to few keys the on-disk file/record
      count stays bounded (does not grow with every append); data intact after reopen.
- [x] reload after a restart does **not** re-append (log length stays stable across reopen).
- [x] malformed trailing line is skipped on load (simulate a torn write), map still loads.
- [x] a pre-existing foreign/binary file (e.g. old MapDB bytes) loads as empty and is
      rewritten clean on load; subsequent puts persist and reload correctly (migration).
- [x] identifiers containing newline/quote/tab round-trip correctly (Jackson escaping).
- [x] concurrent puts from multiple threads all persist and reload (basic thread-safety).
- [x] `close()` is idempotent; the file handle is released.

Integration tests — blacklist (in `integration-test`/existing blacklist test location):
- [x] `LocalPersistentBlacklist`: invalidate → `isInvalidated` true; unknown id → false.
- [x] TTL expiry: after `cleanup()` past expiry, `isInvalidated` returns false.
- [x] Persistence across "restart": new instance on the same file still reports a
      previously invalidated (non-expired) id as invalidated.
- [x] `cleanup()` compacts and rebuilds the bloom filter (existing behaviour preserved).
- [x] Existing blacklist integration tests (now via injected `InMemoryPersistentMap`) still pass.

## Rollout checklist
- [x] Create package `org.owasp.oag.persistentmap` with the three types + Javadoc on all
      public/protected/package-private members (per working directives).
- [x] Write unit tests (above) — red first.
- [x] Implement `PersistentMap`, `InMemoryPersistentMap`, `FilePersistentMap` — green.
- [x] Refactor `LocalPersistentBlacklist` to constructor injection; fix the `Charsets` import.
- [x] Update callers: `OAGBeanConfiguration`, `LocalPersistentBlacklistTest`,
      `IntegrationTestConfig`; delete `LocalInMemoryBlacklist`.
- [x] Keep `application.yaml` `session-blacklist-file: "session-blacklist.db"` unchanged.
- [x] Remove `mapdb` from `build.gradle`; run `./gradlew dependencies` to confirm
      `org.mapdb:*`, `net.jpountz.lz4`, `org.mapdb:elsa` are gone.
- [x] `./gradlew build` — all unit + integration tests pass.
- [x] Update documentation (setup / session-blacklist section) with the new file format, and
      add glossar entries for PersistentMap / compaction / tombstone if the project maintains one.
- [x] Mark this plan finished.

Suggested green-at-each-step sequencing (per the "small increments" directive): introduce the
`persistentmap` package + its tests (green) → refactor `LocalPersistentBlacklist` to constructor
injection and rewire callers/tests to `FilePersistentMap`/`InMemoryPersistentMap` (green) →
remove `mapdb` from `build.gradle` and fix the `Charsets` import (green). Each step keeps the
tree building.

## Definition of Done
- MapDB and its transitive deps no longer on the classpath.
- Session blacklist behaviour (invalidate, check, TTL expiry, persistence across restart,
  bloom-filter fronting) unchanged and covered by tests.
- Documentation updated; this plan marked finished.

## Out of scope / follow-ups
- A distributed/Redis-backed blacklist for multi-instance deployments (separate iteration).
- Scheduling `SessionBlacklist.cleanup()` on the existing `cleanupScheduler`. It is currently
  only called at construction (pre-existing gap — MapDB behaved the same), so expired entries
  are pruned from memory/disk only at restart. Auto-compaction already bounds file size, so
  this is a correctness-of-expiry improvement, not required to remove MapDB. Recommended as a
  fast follow-up.
- Per-append `fsync` (stronger than the compaction-time `fsync` this plan already specifies).
