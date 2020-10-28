package ch.gianlucafrei.nellygateway.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapTreeUpdater {

    public static LinkedHashMap<String, Object> updateMap(
            LinkedHashMap<String, Object> original,
            LinkedHashMap<String, Object> update)
    {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        // Copy original
        for(String key : original.keySet())
        {
            result.put(key, original.get(key));
        }

        // update values
        Set<Map.Entry<String, Object>> entries = update.entrySet();
        for(Map.Entry<String, Object> entry : entries)
        {
            if(entry.getValue() instanceof Map)
            {
                // update recursively
                Object innerOriginal = (LinkedHashMap<String, Object>) original.get(entry.getKey());
                Object innerUpdate = entry.getValue();

                LinkedHashMap<String, Object> updatedInner = updateMap(
                        (LinkedHashMap<String, Object>) innerOriginal,
                        (LinkedHashMap<String, Object>) innerUpdate);

                result.put(entry.getKey(), updatedInner);
            }
            else{
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}
