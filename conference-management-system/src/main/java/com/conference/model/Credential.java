package com.conference.model;

public class Credential {

    private int userId;
    private String passwordHash;

    public Credential(int userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}