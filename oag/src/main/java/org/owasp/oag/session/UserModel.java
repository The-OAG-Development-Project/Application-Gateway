package org.owasp.oag.session;

import java.util.HashMap;
import java.util.Objects;

/**
 * The userModel is the main representation of the user identity in OAG.
 * The loginProvider issues a UserModel after a successful login. It is then stored within the user session.
 * The userMapper is responsible for transporting information from the user model to the downstream service.
 *
 * The only required info is a unique user id. For all other information about the user email, phone, roles, ...
 * the user mappings can be used which is effectively a map of String->String which can represent arbitrary information.
 *
 * Meta information about the user authentication (login time, login provider etc.) are stores within the Session object
 * which also stores the user model.
 */
public class UserModel {

    private String id;
    private HashMap<String, String> mappings;

    /**
     * Creates a new empty user model. Should only be used for deserializing.
     */
    public UserModel() {

        this.mappings = new HashMap<>();
    }

    /**
     * Creates a new user model with a user-id.
     * @param id
     */
    public UserModel(String id) {
        this.id = id;
        this.mappings = new HashMap<>();
    }

    /**
     * Gets the unique id of the user
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Should only be used for deserializing
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the user mappings map
     * @return
     */
    public HashMap<String, String> getMappings() {
        return mappings;
    }

    /**
     * Should only be used for deserializing
     * @param id
     */
    public void setMappings(HashMap<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Sets a specific user mapping
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        mappings.put(key, value);
    }

    /**
     * Gets the value of a user mapping
     * @param key
     * @return
     */
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