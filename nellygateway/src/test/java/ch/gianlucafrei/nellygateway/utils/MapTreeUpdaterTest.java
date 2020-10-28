package ch.gianlucafrei.nellygateway.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;

class MapTreeUpdaterTest {

    @Test
    void contextLoads() {

        LinkedHashMap<String, Object> orginal = new LinkedHashMap<>();
        LinkedHashMap<String, Object> orginalInner = new LinkedHashMap<>();
        orginal.put("a", "original");
        orginal.put("b", "original");
        orginalInner.put("a", "original");
        orginalInner.put("b", "original");
        orginal.put("inner", orginalInner);

        LinkedHashMap<String, Object> update = new LinkedHashMap<>();
        LinkedHashMap<String, Object> updateInner = new LinkedHashMap<>();
        update.put("b", "update");
        updateInner.put("b", "update");
        update.put("inner", updateInner);

        // ACT
        LinkedHashMap<String, Object> updated = MapTreeUpdater.updateMap(orginal, update);

        // ASSERT
        LinkedHashMap<String, Object> updatedInner = (LinkedHashMap<String, Object>) updated.get("inner");
        assertEquals("original", updated.get("a"));
        assertEquals("update", updated.get("b"));
        assertEquals("original", updatedInner.get("a"));
        assertEquals("update", updatedInner.get("b"));


    }
}