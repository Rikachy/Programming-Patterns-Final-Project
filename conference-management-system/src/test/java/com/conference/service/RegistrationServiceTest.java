package com.conference.service;

import com.conference.database.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationServiceTest {

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
    void checkTestDatabasePath() {
        System.out.println("Test database: " + ConnectionFactory.URL);
        System.out.println("Working directory: "
                + System.getProperty("user.dir"));
        assertTrue(true);
    }

    @Test
    void cancellingNonexistentRegistrationReturnsFalse() throws Exception {

        RegistrationService service = new RegistrationService();

        boolean cancelled = service.cancelRegistration(-1);

        assertFalse(cancelled);
    }


    @Test
    void cancellingConfirmedRegistrationPromotesFirstWaitingParticipant()
            throws Exception {

        RegistrationService service = new RegistrationService();

        assertTrue(service.cancelRegistration(1));

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("CANCELLED", dao.findById(1).getStatus());
        assertEquals("CONFIRMED", dao.findById(3).getStatus());
        assertEquals("WAITLISTED", dao.findById(4).getStatus());
    }


    @Test
    void fifoSkipsParticipantWithSchedulingConflict() throws Exception {

        RegistrationService service = new RegistrationService();

        // Give user 4 a confirmed registration that overlaps
        // with the Java workshop.
        try (java.sql.Connection connection =
                     com.conference.database.ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                INSERT INTO Conferences
                (conference_id, title, start_time, end_time, capacity)
                VALUES (99, 'Overlapping Conference',
                        '2026-12-10 10:00:00',
                        '2026-12-10 11:00:00', 10)
                """);

            statement.executeUpdate("""
                INSERT INTO Registrations
                (user_id, conference_id, status)
                VALUES (4, 99, 'CONFIRMED')
                """);
        }

        // Cancelling #1 should skip user 4 and promote user 5.
        assertTrue(service.cancelRegistration(1));

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("CANCELLED", dao.findById(1).getStatus());
        assertEquals("WAITLISTED", dao.findById(3).getStatus());
        assertEquals("CONFIRMED", dao.findById(4).getStatus());
    }


}