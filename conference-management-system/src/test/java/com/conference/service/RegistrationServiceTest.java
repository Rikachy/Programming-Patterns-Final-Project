package com.conference.service;

import com.conference.database.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

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


    @Test
    void participantCanRegisterForAvailableConference() throws Exception {
        RegistrationService service = new RegistrationService();

        int registrationId = service.registerParticipant(3, 2);

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("CONFIRMED", dao.findById(registrationId).getStatus());
    }

    @Test
    void duplicateRegistrationIsRejected() throws Exception {
        RegistrationService service = new RegistrationService();

        assertThrows(IllegalArgumentException.class,
                () -> service.registerParticipant(2, 1));
    }

    @Test
    void fullConferenceAddsParticipantToWaitingList() throws Exception {
        RegistrationService service = new RegistrationService();

        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
            INSERT INTO Users
            (user_id, full_name, email, age, role)
            VALUES (6, 'Test Participant',
                    'testcapacity@example.com', 25, 'PARTICIPANT')
            """);
        }

        int registrationId = service.registerParticipant(6, 1);

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("WAITLISTED", dao.findById(registrationId).getStatus());

        assertTrue(service.getWaitingList(1).stream()
                .anyMatch(entry -> entry.getRegistrationId() == registrationId));
    }


    @Test
    void overlappingRegistrationIsRejected() throws Exception {
        RegistrationService service = new RegistrationService();

        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
            INSERT INTO Conferences
            (conference_id, title, start_time, end_time, capacity)
            VALUES (99, 'Overlapping Conference',
                    '2026-12-10 10:00:00',
                    '2026-12-10 11:00:00', 10)
            """);
        }

        assertThrows(IllegalArgumentException.class,
                () -> service.registerParticipant(2, 99));
    }

    @Test
    void cancellingWaitlistedRegistrationDoesNotPromoteAnyone()
            throws Exception {

        RegistrationService service = new RegistrationService();

        assertTrue(service.cancelRegistration(3));

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("CANCELLED", dao.findById(3).getStatus());
        assertEquals("WAITLISTED", dao.findById(4).getStatus());
        assertEquals("CONFIRMED", dao.findById(1).getStatus());
        assertEquals("CONFIRMED", dao.findById(2).getStatus());

        assertFalse(service.getWaitingList(1).stream()
                .anyMatch(entry -> entry.getRegistrationId() == 3));
    }

    @Test
    void cancellingAlreadyCancelledRegistrationReturnsFalse()
            throws Exception {

        RegistrationService service = new RegistrationService();

        assertTrue(service.cancelRegistration(1));
        assertFalse(service.cancelRegistration(1));

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("CANCELLED", dao.findById(1).getStatus());
        assertEquals("CONFIRMED", dao.findById(3).getStatus());
        assertEquals("WAITLISTED", dao.findById(4).getStatus());
    }


    @Test
    void cancellationRollsBackWhenPromotionFails() throws Exception {

        RegistrationService service = new RegistrationService();

        // Simulate a database failure during promotion.
        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute("""
            CREATE TRIGGER fail_promotion
            BEFORE UPDATE ON Registrations
            WHEN OLD.status = 'WAITLISTED'
                 AND NEW.status = 'CONFIRMED'
            BEGIN
                SELECT RAISE(ABORT, 'Simulated promotion failure');
            END;
            """);
        }

        assertThrows(java.sql.SQLException.class,
                () -> service.cancelRegistration(1));

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        // Verify that the cancellation was rolled back.
        assertEquals("CONFIRMED", dao.findById(1).getStatus());
        assertEquals("WAITLISTED", dao.findById(3).getStatus());
        assertEquals("WAITLISTED", dao.findById(4).getStatus());

        assertTrue(service.getWaitingList(1).stream()
                .anyMatch(entry -> entry.getRegistrationId() == 3));
    }


    @Test
    void registrationRollsBackWhenWaitingListInsertFails()
            throws Exception {

        RegistrationService service = new RegistrationService();

        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
            INSERT INTO Users
            (user_id, full_name, email, age, role)
            VALUES (6, 'Rollback Participant',
                    'rollback@example.com', 25, 'PARTICIPANT')
            """);

            statement.execute("""
            CREATE TRIGGER fail_waiting_list
            BEFORE INSERT ON WaitingList
            BEGIN
                SELECT RAISE(ABORT, 'Simulated waiting list failure');
            END;
            """);
        }

        assertThrows(java.sql.SQLException.class,
                () -> service.registerParticipant(6, 1));

        // Verify that no incomplete registration remains.
        try (java.sql.Connection connection =
                     ConnectionFactory.getConnection();
             java.sql.Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("""
             SELECT COUNT(*) FROM Registrations
             WHERE user_id = 6 AND conference_id = 1
             """)) {

            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }

        assertEquals(2, service.getWaitingList(1).size());
    }


    @Test
    void participantCanRegisterAgainAfterCancellation()
            throws Exception {

        RegistrationService service = new RegistrationService();

        assertTrue(service.cancelRegistration(1));

        int registrationId = service.registerParticipant(2, 1);

        com.conference.dao.RegistrationDao dao =
                new com.conference.dao.RegistrationDao();

        assertEquals("WAITLISTED",
                dao.findById(registrationId).getStatus());
    }


    @Test
    void testSchedulingConflictUsingSeedData() throws SQLException {

        RegistrationService service = new RegistrationService();

        assertThrows(IllegalArgumentException.class,
                () -> service.registerParticipant(2, 4));
    }






}