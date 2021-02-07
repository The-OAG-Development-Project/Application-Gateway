package ch.gianlucafrei.nellygateway.services.blacklist;

import ch.gianlucafrei.nellygateway.GlobalClockSource;
import ch.gianlucafrei.nellygateway.utils.ReactiveUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import kotlin.text.Charsets;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * Uses a local persisted map to store invalidated identifiers locally.
 * Additionally a bloom filter is used to check for identifiers that are definitely not in the blacklist within o(1)
 */
public class LocalPersistentBlacklist implements SessionBlacklist {

    protected static final int EXPECTED_INSERTIONS = 100000;
    protected static final double EXPECTED_BLOOM_FILTER_FALSE_POSITIVES = 0.001;
    private static final Logger log = LoggerFactory.getLogger(LocalPersistentBlacklist.class);
    protected DB db;
    protected HTreeMap<String, Integer> blacklist;
    protected BloomFilter<CharSequence> bloomFilter;

    private final GlobalClockSource clockSource;

    public LocalPersistentBlacklist(GlobalClockSource clockSource, String filename) {

        this.clockSource = clockSource;
        initDb(filename);

        blacklist = db.hashMap("session-blacklist", Serializer.STRING, Serializer.INTEGER)
                .createOrOpen();

        // Populate bloom filter with all entries from blacklist
        cleanup().block();
    }

    protected void initDb(String filename) {

        this.db = DBMaker.fileDB(filename)
                .transactionEnable()
                .closeOnJvmShutdown()
                .make();
    }

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

    public void deleteAllEntries() {

        blacklist.clear();
        cleanup().block();
    }

    @Override
    public Mono<Void> invalidateSession(String identifier, int ttl) {

        log.trace("Invalidate identifier {}", identifier);
        return ReactiveUtils.runBlockingProcedure(() -> invalidateSessionBlocking(identifier, ttl));
    }

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

    @Override
    public Mono<Boolean> isInvalidated(String identifier) {

        log.trace("Check if identifier {} is in bloom filter", identifier);

        // Very fast false if the identifier is not in the bloom filter
        if (!bloomFilter.mightContain(identifier)) {

            log.trace("Identifier {} is not in bloom filter", identifier);
            return Mono.just(false);
        }

        log.trace("Identifier {} is in bloom filter, start db lookup asynchronously", identifier);
        return ReactiveUtils.runBlockingProcedure(() -> checkIfInDbBlocking(identifier));

    }

    private boolean checkIfInDbBlocking(String identifier) {

        Object o = blacklist.getOrDefault(identifier, null);
        var isInDb = o != null;

        log.trace("Lookup for identifier {} returned {}", identifier, isInDb);
        return isInDb;
    }

    @Override
    public Mono<Void> cleanup() {
        return ReactiveUtils.runBlockingProcedure(() -> cleanupBlocking());
    }

    @Override
    public void close() throws IOException {
        db.close();
    }
}
