package fr.eurecom.appmemorable.models;

import androidx.annotation.NonNull;

import java.util.List;

public class User {
    private String email, name, uid;

    public User(String email, String name, String uid) {
        this.email = email;
        this.name = name;
        this.uid = uid;
    }
    public User(){

    }

    @NonNull
    @Override
    public String toString() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
