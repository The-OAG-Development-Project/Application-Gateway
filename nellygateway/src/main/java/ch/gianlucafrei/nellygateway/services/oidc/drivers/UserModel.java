package ch.gianlucafrei.nellygateway.services.oidc.drivers;

import java.util.HashMap;

public class UserModel {

    private String id;
    private HashMap<String, String> mappings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getMappings() {
        return mappings;
    }

    public void setMappings(HashMap<String, String> mappings) {
        this.mappings = mappings;
    }

    private void put(String key, String value){
        mappings.put(key, value);
    }

    private String get(String key){
        return mappings.get(key);
    }
}