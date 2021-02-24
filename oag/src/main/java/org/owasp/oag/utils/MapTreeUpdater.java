package org.owasp.oag.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapTreeUpdater {

    public static Map<String, Object> updateMap(Object original, Object update) {

        if (original instanceof Map && update instanceof Map) {
            ObjectMapper om = new ObjectMapper();
            TypeReference<LinkedHashMap<String, Object>> mapType = new TypeReference<>() {
            };
            Map<String, Object> originalMap = om.convertValue(original, mapType);
            Map<String, Object> updateMap = om.convertValue(update, mapType);

            return updateMapInner(originalMap, updateMap);
        }

        return null;
    }

    private static Map<String, Object> updateMapInner(Map<String, Object> original,
                                                      Map<String, Object> update) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Copy original
        for (String key : original.keySet()) {
            result.put(key, original.get(key));
        }

        // update values
        for (Map.Entry<String, Object> entry : update.entrySet()) {


            if (!original.containsKey(entry.getKey()))
                // Set if value is not present in original
                result.put(entry.getKey(), entry.getValue());
            else if (original.get(entry.getKey()) == null) {
                // Overwrite value in original if it is null
                result.put(entry.getKey(), entry.getValue());
            } else if (entry.getValue() instanceof Map) {

                // update recursively
                Object innerOriginal = original.get(entry.getKey());
                Object innerUpdate = entry.getValue();
                Map<String, Object> updatedInner = updateMap(innerOriginal, innerUpdate);
                result.put(entry.getKey(), updatedInner);
            } else {
                // Set value
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}
