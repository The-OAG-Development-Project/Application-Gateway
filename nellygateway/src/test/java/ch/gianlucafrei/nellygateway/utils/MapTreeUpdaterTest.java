package ch.gianlucafrei.nellygateway.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapTreeUpdaterTest {

    @Test
    void testMapUpdater() {

        Map<String, Object> orginal = new LinkedHashMap<>();
        Map<String, Object> orginalInner = new LinkedHashMap<>();
        orginal.put("a", "original");
        orginal.put("b", "original");
        orginalInner.put("a", "original");
        orginalInner.put("b", "original");
        orginal.put("inner", orginalInner);

        Map<String, Object> update = new LinkedHashMap<>();
        Map<String, Object> updateInner = new LinkedHashMap<>();
        update.put("b", "update");
        updateInner.put("b", "update");
        update.put("inner", updateInner);

        // ACT
        Map<String, Object> updated = MapTreeUpdater.updateMap(orginal, update);

        // ASSERT
        ObjectMapper om = new ObjectMapper();
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {
        };
        Map<String, Object> updatedInner = om.convertValue(updated.get("inner"), mapType);
        assertEquals("original", updated.get("a"));
        assertEquals("update", updated.get("b"));
        assertEquals("original", updatedInner.get("a"));
        assertEquals("update", updatedInner.get("b"));


    }

    @Test
    void testMapUpaterOverwritesMissingValues() {

        Map<String, Object> orginal = new LinkedHashMap<>();
        orginal.put("a", "original");
        orginal.put("inner", null);

        Map<String, Object> update = new LinkedHashMap<>();
        Map<String, Object> updateInner = new LinkedHashMap<>();
        updateInner.put("inner-b", "foo");
        update.put("inner", updateInner);

        // ACT
        Map<String, Object> updated = MapTreeUpdater.updateMap(orginal, update);

        // ASSERT
        ObjectMapper om = new ObjectMapper();
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {
        };
        Map<String, Object> updatedInner = om.convertValue(updated.get("inner"), mapType);
        assertEquals("foo", updatedInner.get("inner-b"));
    }
}