package ch.gianlucafrei.nellygateway.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

class MapTreeUpdaterTest {

    @Test
    void contextLoads() {

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
        TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {};
        Map<String, Object> updatedInner = om.convertValue(updated.get("inner"), mapType);
        assertEquals("original", updated.get("a"));
        assertEquals("update", updated.get("b"));
        assertEquals("original", updatedInner.get("a"));
        assertEquals("update", updatedInner.get("b"));


    }
}