package org.owasp.oag.persistentmap;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A {@link PersistentMap} that keeps all entries in memory only. Nothing is persisted, so the
 * data is lost when the instance is discarded. Used as a lightweight test double.
 *
 * @param <V> the value type
 */
public class InMemoryPersistentMap<V> implements PersistentMap<V> {

    /** In-memory backing store. */
    private final ConcurrentHashMap<String, V> map = new ConcurrentHashMap<>();

    @Override
    public V get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, V value) {
        map.put(key, Objects.requireNonNull(value, "value must not be null"));
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        return map.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void close() {
        // Nothing to release for an in-memory map.
    }
}
