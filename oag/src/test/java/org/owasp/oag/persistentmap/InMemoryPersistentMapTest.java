package org.owasp.oag.persistentmap;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link InMemoryPersistentMap}.
 */
class InMemoryPersistentMapTest {

    @Test
    void putGetRemoveContainsSizeClear() {
        var map = new InMemoryPersistentMap<Integer>();

        map.put("a", 1);
        map.put("b", 2);

        assertEquals(1, map.get("a"));
        assertTrue(map.containsKey("b"));
        assertEquals(2, map.size());

        map.remove("a");
        assertNull(map.get("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());

        map.clear();
        assertEquals(0, map.size());
    }

    @Test
    void entrySetIsAnImmutableSnapshot() {
        var map = new InMemoryPersistentMap<Integer>();
        map.put("a", 1);

        var snapshot = map.entrySet();
        map.put("b", 2);

        // The snapshot does not reflect later modifications and is immutable.
        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(Map.entry("c", 3)));
    }

    @Test
    void putRejectsNullValue() {
        var map = new InMemoryPersistentMap<Integer>();
        assertThrows(NullPointerException.class, () -> map.put("a", null));
    }
}
