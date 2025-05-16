package org.owasp.oag.services.blacklist;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import kotlin.text.Charsets;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.owasp.oag.infrastructure.GlobalClockSource;
import org.owasp.oag.utils.LoggingUtils;
import org.owasp.oag.utils.ReactiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * Uses a local persisted map to store invalidated identifiers locally.
 * Additionally a bloom filter is used to check for identifiers that are definitely not in the blacklist within o(1).
 * This implementation provides persistent storage of blacklisted session identifiers across application restarts.
 */
public class LocalPersistentBlacklist implements SessionBlacklist {

    /**
     * Expected number of insertions for the bloom filter.
     */
    protected static final int EXPECTED_INSERTIONS = 100000;
    
    /**
     * Expected false positive rate for the bloom filter.
     */
    protected static final double EXPECTED_BLOOM_FILTER_FALSE_POSITIVES = 0.001;
    
    private static final Logger log = LoggerFactory.getLogger(LocalPersistentBlacklist.class);
    
    /**
     * Database instance for persistent storage.
     */
    protected DB db;
    
    /**
     * Map storing blacklisted session identifiers and their expiration times.
     */
    protected HTreeMap<String, Integer> blacklist;
    
    /**
     * Bloom filter for efficient negative lookups.
     */
    protected BloomFilter<CharSequence> bloomFilter;

    /**
     * Clock source for time-based operations.
     */
    private final GlobalClockSource clockSource;

    /**
     * Creates a new LocalPersistentBlacklist with the specified clock source and database file.
     *
     * @param clockSource The clock source for time-based operations
     * @param filename The file name for the persistent database
     */
    public LocalPersistentBlacklist(GlobalClockSource clockSource, String filename) {

        this.clockSource = clockSource;
        initDb(filename);

        blacklist = db.hashMap("session-blacklist", Serializer.STRING, Serializer.INTEGER)
                .createOrOpen();

        // Populate bloom filter with all entries from blacklist
        cleanup().block();
    }

    /**
     * Initializes the persistent database.
     *
     * @param filename The file name for the persistent database
     */
    protected void initDb(String filename) {

        this.db = DBMaker.fileDB(filename)
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
    }

    /**
     * Performs a cleanup operation in a blocking manner.
     * Removes expired entries from the blacklist and rebuilds the bloom filter.
     */
    public void cleanupBlocking() {

        int currentTimeSeconds = clockSource.getEpochSeconds();

        // Clean up expired entries
        blacklist.getEntries()
                .stream().filter(e -> {
            var expiry = e.getValue();
            return expiry < currentTimeSeconds;
        })
                .forEach(e -> blacklist.remove(e.getKey()));
        db.commit();

        // Populate bloom filter
        var size = blacklist.size();
        var newFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), size + EXPECTED_INSERTIONS, EXPECTED_BLOOM_FILTER_FALSE_POSITIVES);
        blacklist.getEntries()
                .forEach(e -> newFilter.put(e.getKey()));

        // set new filter
        bloomFilter = newFilter;
    }

    /**
     * Deletes all entries from the blacklist.
     * This is primarily used for testing.
     */
    public void deleteAllEntries() {

        blacklist.clear();
        cleanup().block();
    }

    /**
     * Invalidates a session identifier by adding it to the blacklist.
     *
     * @param identifier The session identifier to invalidate
     * @param ttl The time to live in seconds for the invalidation
     * @return A Mono that completes when the operation is done
     */
    @Override
    public Mono<Void> invalidateSession(String identifier, int ttl) {

        return LoggingUtils.contextual(() -> log.trace("Invalidate identifier {}", identifier))
                .then(ReactiveUtils.runBlockingProcedure(() -> invalidateSessionBlocking(identifier, ttl)));
    }

    /**
     * Invalidates a session identifier in a blocking manner.
     * Adds the identifier to both the persistent database and the bloom filter.
     *
     * @param identifier The session identifier to invalidate
     * @param ttl The time to live in seconds for the invalidation
     */
    private void invalidateSessionBlocking(String identifier, int ttl) {

        // Put entry in db
        int currentTimeSeconds = clockSource.getEpochSeconds();
        int expireTime = currentTimeSeconds + ttl;

        blacklist.put(identifier, expireTime);
        db.commit();

        //Add to bloom filter
        bloomFilter.put(identifier);

        log.trace("Stored identifier {} in blacklist db", identifier);
    }

    /**
     * Checks if a session identifier is invalidated (blacklisted).
     * Uses a bloom filter for efficient negative lookups.
     *
     * @param identifier The session identifier to check
     * @return A Mono containing true if the identifier is invalidated, false otherwise
     */
    @Override
    public Mono<Boolean> isInvalidated(String identifier) {

        // Very fast false if the identifier is not in the bloom filter
        if (!bloomFilter.mightContain(identifier)) {

            return Mono.just(false)
                    .doOnEach(LoggingUtils.logOnNext((u) -> log.trace("Identifier {} is not in bloom filter", identifier)));
        }

        return LoggingUtils.contextual(() -> log.trace("Identifier {} is in bloom filter, start db lookup asynchronously", identifier))
                .then(ReactiveUtils.runBlockingProcedure(() -> checkIfInDbBlocking(identifier)));
    }

    /**
     * Checks if a session identifier is in the database in a blocking manner.
     *
     * @param identifier The session identifier to check
     * @return true if the identifier is in the database, false otherwise
     */
    private boolean checkIfInDbBlocking(String identifier) {

        Object o = blacklist.getOrDefault(identifier, null);
        var isInDb = o != null;

        log.trace("Lookup for identifier {} returned {}", identifier, isInDb);
        return isInDb;
    }

    /**
     * Performs a cleanup operation to remove expired entries from the blacklist.
     *
     * @return A Mono that completes when the operation is done
     */
    @Override
    public Mono<Void> cleanup() {
        return ReactiveUtils.runBlockingProcedure(() -> cleanupBlocking());
    }

    /**
     * Closes the database when the blacklist is no longer needed.
     *
     * @throws IOException if an I/O error occurs when closing the database
     */
    @Override
    public void close() throws IOException {
        db.close();
    }
}
