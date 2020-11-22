package ch.gianlucafrei.nellygateway.services.oidc.drivers;

import java.util.HashMap;

public class UserModel {

    private String id;
    private HashMap<String, String> mappings;

    public UserModel() {

        this.mappings = new HashMap<>();
    }

    public UserModel(String id) {
        this.id = id;
        this.mappings = new HashMap<>();
    }

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

    public void set(String key, String value){
        mappings.put(key, value);
    }

    public String get(String key){
        return mappings.get(key);
    }
}