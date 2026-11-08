package com.conference.model;

public class WaitingList {

    private int waitingId;
    private int registrationId;
    private String joinedAt;

    public WaitingList(int waitingId, int registrationId, String joinedAt) {
        this.waitingId = waitingId;
        this.registrationId = registrationId;
        this.joinedAt = joinedAt;
    }



    public int getWaitingId() {
        return waitingId;
    }

    public void setWaitingId(int waitingId) {
        this.waitingId = waitingId;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}