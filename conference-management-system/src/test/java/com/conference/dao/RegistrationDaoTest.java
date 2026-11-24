package com.conference.dao;

import com.conference.model.Registration;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import com.conference.model.WaitingList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.conference.database.ConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationDaoTest {

    @BeforeEach
    void setUp() throws Exception {
        ConnectionFactory.URL = "jdbc:sqlite:conference-test.db";
        com.conference.TestDatabaseInitializer.initialize();
    }

    @AfterEach
    void tearDown() {
        ConnectionFactory.URL = "jdbc:sqlite:conference.db";
    }


    @Test
    void testRegistrationModel() {

        Registration registration = new Registration(
                1, 2, 1, "CONFIRMED", "2026-11-03 10:00:00");

        assertEquals(1, registration.getRegistrationId());
        assertEquals(2, registration.getUserId());
        assertEquals(1, registration.getConferenceId());
        assertEquals("CONFIRMED", registration.getStatus());
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


    @Test
    void testCreateRegistration() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        int registrationId =
                dao.createRegistration(3, 2, "CONFIRMED");

        Registration registration = dao.findById(registrationId);

        assertNotNull(registration);
        assertEquals(3, registration.getUserId());
        assertEquals(2, registration.getConferenceId());
        assertEquals("CONFIRMED", registration.getStatus());
    }

    @Test
    void testUpdateRegistrationStatus() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        assertEquals(1,
                dao.updateRegistrationStatus(1, "CANCELLED"));

        assertEquals("CANCELLED", dao.findById(1).getStatus());
    }

    @Test
    void testDeleteRegistration() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        int registrationId =
                dao.createRegistration(3, 2, "CONFIRMED");

        assertEquals(1, dao.deleteRegistration(registrationId));
        assertNull(dao.findById(registrationId));
    }

    @Test
    void testFindAllRegistrations() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        List<Registration> registrations = dao.findAll();

        assertEquals(5, registrations.size());
    }

    @Test
    void testCountConfirmedRegistrations() throws SQLException {

        RegistrationDao dao = new RegistrationDao();

        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection()) {

            assertEquals(2, dao.countConfirmed(connection, 1));
            assertEquals(1, dao.countConfirmed(connection, 2));
            assertEquals(0, dao.countConfirmed(connection, 3));
        }
    }


    @Test
    void testDuplicateRegistrationConstraint() {

        RegistrationDao dao = new RegistrationDao();

        assertThrows(SQLException.class,
                () -> dao.createRegistration(2, 1, "CONFIRMED"));
    }

    @Test
    void testInvalidStatusConstraint() {

        RegistrationDao dao = new RegistrationDao();

        assertThrows(SQLException.class,
                () -> dao.createRegistration(3, 2, "INVALID"));
    }

    @Test
    void testInvalidUserForeignKey() {

        RegistrationDao dao = new RegistrationDao();

        assertThrows(SQLException.class,
                () -> dao.createRegistration(999, 1, "CONFIRMED"));
    }

    @Test
    void testInvalidConferenceForeignKey() {

        RegistrationDao dao = new RegistrationDao();

        assertThrows(SQLException.class,
                () -> dao.createRegistration(2, 999, "CONFIRMED"));
    }


    @Test
    void testInitialWaitingListPositions() throws SQLException {

        WaitingListDao dao = new WaitingListDao();

        assertEquals(1, dao.getWaitingListPosition(1, 3));
        assertEquals(2, dao.getWaitingListPosition(1, 4));
    }

    @Test
    void testWaitingListPositionAfterPromotion() throws SQLException {

        WaitingListDao dao = new WaitingListDao();

        com.conference.service.RegistrationService service =
                new com.conference.service.RegistrationService();

        assertTrue(service.cancelRegistration(1));

        assertEquals(-1, dao.getWaitingListPosition(1, 3));
        assertEquals(1, dao.getWaitingListPosition(1, 4));
    }

    @Test
    void testWaitingListPositionAfterCancellation() throws SQLException {

        WaitingListDao dao = new WaitingListDao();

        com.conference.service.RegistrationService service =
                new com.conference.service.RegistrationService();

        assertTrue(service.cancelRegistration(3));

        assertEquals(-1, dao.getWaitingListPosition(1, 3));
        assertEquals(1, dao.getWaitingListPosition(1, 4));
    }



}