package com.conference.dao;

import com.conference.model.Registration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationDaoTest {

    @Test
    void testRegistrationModel() {

        Registration registration = new Registration(
                1, 2, 1, "CONFIRMED", "2026-11-03 10:00:00"
        );

        assertEquals(1, registration.getRegistrationId());
        assertEquals(2, registration.getUserId());
        assertEquals(1, registration.getConferenceId());
        assertEquals("CONFIRMED", registration.getStatus());
    }
}