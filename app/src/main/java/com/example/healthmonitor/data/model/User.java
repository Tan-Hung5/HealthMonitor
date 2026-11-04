package com.example.healthmonitor.data.model;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String phonenumber;
    private String birthdate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String name, String email, String phonenumber, String birthdate) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phonenumber = phonenumber;
        this.birthdate = birthdate;
    }

    public String getUserId() { 
        return userId; 
    }
    public void setUserId(Object userId) { 
        if (userId instanceof Long) {
            this.userId = String.valueOf(userId);
        } else if (userId instanceof String) {
            this.userId = (String) userId;
        } else if (userId != null) {
            this.userId = userId.toString();
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
}
