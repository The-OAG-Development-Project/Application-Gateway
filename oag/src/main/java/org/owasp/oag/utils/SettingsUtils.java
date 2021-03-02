package org.owasp.oag.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.Map;

public class SettingsUtils {

    public static <T> T settingsFromMap(Map<String, Object> map, Class clazz){

        try{
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            String combinedConfigStr = om.writeValueAsString(map);
            T config = (T)om.readValue(combinedConfigStr, clazz);
            return config;
        }
        catch (JsonProcessingException ex){

            throw new RuntimeException("Could not load settings", ex);
        }
    }
}
