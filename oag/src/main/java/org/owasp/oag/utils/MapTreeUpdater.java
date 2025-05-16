package org.owasp.oag.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for merging and updating hierarchical map structures.
 * Provides functionality to recursively merge two maps, with values from the update map
 * taking precedence over values from the original map.
 */
public class MapTreeUpdater {

    /**
     * Updates a map with values from another map, performing a deep merge.
     * If both objects are maps, converts them using Jackson and performs a recursive merge.
     *
     * @param original The original object to be updated
     * @param update The object containing the updates
     * @return A new map containing the merged values, or null if inputs are not maps
     */
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

    /**
     * Performs the actual recursive map merging operation.
     * This method handles the deep merging of nested map structures.
     *
     * Rules for merging:
     * - If a key exists only in the update map, it is added to the result
     * - If a key exists in both maps and the original value is null, the update value is used
     * - If a key exists in both maps and both values are maps, they are recursively merged
     * - Otherwise, the update value overwrites the original value
     *
     * @param original The original map with base values
     * @param update The map with values that should override or extend the original
     * @return A new map containing the merged result
     */
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
