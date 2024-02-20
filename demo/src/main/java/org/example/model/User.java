package org.example.model;

import org.apache.avro.reflect.Nullable;

/**
 * Make sure to annotate your class members with @Nullable from org.apache.avro.reflect to handle nullable fields in the Avro schema. The Avro library uses reflection to serialize and deserialize objects based on the Avro schema.
 */

@SuppressWarnings("all")
public class User {
    private int id;
    private String name;
    private String email;

    public User() {
        // Avro requires a default constructor
    }

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}