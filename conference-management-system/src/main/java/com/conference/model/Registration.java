package com.conference.model;

public class Registration {

    private int registrationId;
    private int userId;
    private int conferenceId;
    private String status;
    private String registeredAt;

    public Registration(int registrationId, int userId,
                        int conferenceId, String status,
                        String registeredAt) {

        this.registrationId = registrationId;
        this.userId = userId;
        this.conferenceId = conferenceId;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(int conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }


}