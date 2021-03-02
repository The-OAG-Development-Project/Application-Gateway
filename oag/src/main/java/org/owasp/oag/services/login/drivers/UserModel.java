package org.owasp.oag.services.login.drivers;

import java.util.HashMap;
import java.util.Objects;

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

    public void set(String key, String value) {
        mappings.put(key, value);
    }

    public String get(String key) {
        return mappings.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(getId(), userModel.getId()) &&
                Objects.equals(getMappings(), userModel.getMappings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMappings());
    }
}