package org.owasp.oag.config.configuration;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultConfigValues {

    public static UserMappingConfig userMapping(){

        var userMapping = new UserMappingConfig();
        userMapping.setType("no-mapping");
        userMapping.setSettings(new HashMap<>());
        return userMapping;
    }

    public static List<String> csrfSafeMethods(){

        return Lists.asList("GET", new String[]{"HEAD", "OPTIONS"});
    }

    public static Map<String, String> responseHeaders() {

        return new HashMap<>();
    }
}
