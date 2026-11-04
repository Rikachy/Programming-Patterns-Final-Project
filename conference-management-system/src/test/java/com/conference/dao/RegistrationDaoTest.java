package com.conference.dao;

import com.conference.model.Registration;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import com.conference.model.WaitingList;
import java.util.List;

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

    @Test
    void testCountConfirmedRegistrations() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        int count = dao.countConfirmedRegistrations(1);

        assertEquals(2, count);
    }

    @Test
    void testWaitingListOrder() throws SQLException {

        WaitingListDao dao = new WaitingListDao();

        List<WaitingList> waitingList = dao.getWaitingList(1);

        assertEquals(2, waitingList.size());

        assertEquals(3, waitingList.get(0).getRegistrationId());
        assertEquals(4, waitingList.get(1).getRegistrationId());
    }

    @Test
    void testFindRegistrationById() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        Registration registration = dao.findById(1);

        assertNotNull(registration);
        assertEquals(2, registration.getUserId());
        assertEquals(1, registration.getConferenceId());
        assertEquals("CONFIRMED", registration.getStatus());
    }


}