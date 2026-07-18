package org.owasp.oag.persistentmap;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

/**
 * A small key-value map with {@code String} keys. Depending on the implementation the entries
 * are kept in memory only or persisted across restarts. Values must never be {@code null}.
 *
 * @param <V> the value type
 */
public interface PersistentMap<V> extends Closeable {

    /**
     * Returns the value stored for the given key.
     *
     * @param key the key to look up
     * @return the stored value, or {@code null} if the key is absent
     */
    V get(String key);

    /**
     * Stores a value for the given key, replacing any previous value. Persistent
     * implementations durably store the entry before returning.
     *
     * @param key   the key
     * @param value the value, must not be {@code null}
     */
    void put(String key, V value);

    /**
     * Removes the entry for the given key if present.
     *
     * @param key the key to remove
     */
    void remove(String key);

    /**
     * Checks whether the map contains an entry for the given key.
     *
     * @param key the key to check
     * @return {@code true} if the key is present
     */
    boolean containsKey(String key);

    /**
     * Returns the number of entries in the map.
     *
     * @return the entry count
     */
    int size();

    /**
     * Removes all entries from the map.
     */
    void clear();

    /**
     * Returns an immutable snapshot of the current entries that is safe to iterate while the
     * map is modified concurrently.
     *
     * @return a snapshot of the entries
     */
    Set<Map.Entry<String, V>> entrySet();
}
