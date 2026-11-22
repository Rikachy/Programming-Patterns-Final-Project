
package com.conference.service;

import com.conference.dao.RegistrationDao;
import com.conference.model.Registration;
import com.conference.dao.WaitingListDao;
import com.conference.model.WaitingList;
import com.conference.dao.ConferenceDao;
import com.conference.model.Conference;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.conference.database.ConnectionFactory;
import java.sql.Connection;
import java.util.List;
import java.sql.SQLException;

public class RegistrationService {

    private RegistrationDao registrationDao = new RegistrationDao();
    private WaitingListDao waitingListDao = new WaitingListDao();
    private ConferenceDao conferenceDao = new ConferenceDao();

    public boolean registrationExists(int registrationId)
            throws SQLException {

        Registration registration = registrationDao.findById(registrationId);

        return registration != null;
    }


    public boolean cancelRegistration(int registrationId)
            throws SQLException {

        Registration registration = registrationDao.findById(registrationId);

        if (registration == null) {
            return false;
        }

        if (registration.getStatus().equals("CANCELLED")) {
            return false;
        }

        registrationDao.updateRegistrationStatus(registrationId, "CANCELLED");

        return true;
    }


    public List<WaitingList> getWaitingList(int conferenceId)
            throws SQLException {

        return waitingListDao.getWaitingList(conferenceId);
    }


    public boolean addToWaitingList(int registrationId)
            throws SQLException {

        Registration registration = registrationDao.findById(registrationId);

        if (registration == null) {
            return false;
        }

        if (!registration.getStatus().equals("WAITLISTED")) {
            return false;
        }

        waitingListDao.addToWaitingList(registrationId);

        return true;
    }


    public boolean isConferenceFull(int conferenceId)
            throws SQLException {

        Conference conference = conferenceDao.findById(conferenceId);

        if (conference == null) {
            return false;
        }

        int confirmedCount = 0;

        for (Registration registration : registrationDao.findAll()) {

            if (registration.getConferenceId() == conferenceId
                    && registration.getStatus().equals("CONFIRMED")) {

                confirmedCount++;
            }
        }

        return confirmedCount >= conference.getCapacity();
    }


    public boolean isAlreadyRegistered(int userId, int conferenceId)
            throws SQLException {

        for (Registration registration : registrationDao.findAll()) {

            if (registration.getUserId() == userId
                    && registration.getConferenceId() == conferenceId
                    && !registration.getStatus().equals("CANCELLED")) {

                return true;
            }
        }

        return false;
    }


    public boolean hasSchedulingConflict(int userId, int conferenceId)
            throws SQLException {

        Conference newConference = conferenceDao.findById(conferenceId);

        if (newConference == null) {
            return false;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime newStart = LocalDateTime.parse(
                newConference.getStartTime(), formatter);

        LocalDateTime newEnd = LocalDateTime.parse(
                newConference.getEndTime(), formatter);

        for (Registration registration : registrationDao.findAll()) {

            if (registration.getUserId() == userId
                    && registration.getStatus().equals("CONFIRMED")) {

                Conference existingConference =
                        conferenceDao.findById(registration.getConferenceId());

                if (existingConference != null) {

                    LocalDateTime existingStart = LocalDateTime.parse(
                            existingConference.getStartTime(), formatter);

                    LocalDateTime existingEnd = LocalDateTime.parse(
                            existingConference.getEndTime(), formatter);

                    if (newStart.isBefore(existingEnd)
                            && newEnd.isAfter(existingStart)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }



    public int registerParticipant(int userId, int conferenceId)
            throws SQLException {

        try (Connection connection = ConnectionFactory.getConnection()) {

            // Lock the database for writing before checking seats.
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA busy_timeout = 5000");
                statement.execute("BEGIN IMMEDIATE");
            }

            try {
                // Check that the participant exists and is eligible.
                String userSql = """
                    SELECT role, age
                    FROM Users
                    WHERE user_id = ?
                    """;

                try (java.sql.PreparedStatement statement =
                             connection.prepareStatement(userSql)) {

                    statement.setInt(1, userId);

                    try (java.sql.ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                    "Participant not found");
                        }

                        if (!"PARTICIPANT".equals(result.getString("role"))) {
                            throw new IllegalArgumentException(
                                    "Only participants can register");
                        }
                    }
                }

                // Get conference details using the SAME connection.
                int capacity;
                String startTime;
                String endTime;

                String conferenceSql = """
                    SELECT capacity, start_time, end_time
                    FROM Conferences
                    WHERE conference_id = ?
                    """;

                try (java.sql.PreparedStatement statement =
                             connection.prepareStatement(conferenceSql)) {

                    statement.setInt(1, conferenceId);

                    try (java.sql.ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                    "Conference not found");
                        }

                        capacity = result.getInt("capacity");
                        startTime = result.getString("start_time");
                        endTime = result.getString("end_time");
                    }
                }

                // Prevent duplicate active registrations.
                String duplicateSql = """
                    SELECT COUNT(*)
                    FROM Registrations
                    WHERE user_id = ?
                      AND conference_id = ?
                      AND status IN ('CONFIRMED', 'WAITLISTED')
                    """;

                try (java.sql.PreparedStatement statement =
                             connection.prepareStatement(duplicateSql)) {

                    statement.setInt(1, userId);
                    statement.setInt(2, conferenceId);

                    try (java.sql.ResultSet result = statement.executeQuery()) {
                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalArgumentException(
                                    "Already registered");
                        }
                    }
                }

                // Check scheduling conflicts with confirmed registrations.
                String conflictSql = """
                    SELECT COUNT(*)
                    FROM Registrations r
                    JOIN Conferences c
                      ON r.conference_id = c.conference_id
                    WHERE r.user_id = ?
                      AND r.status = 'CONFIRMED'
                      AND c.start_time < ?
                      AND c.end_time > ?
                    """;

                try (java.sql.PreparedStatement statement =
                             connection.prepareStatement(conflictSql)) {

                    statement.setInt(1, userId);
                    statement.setString(2, endTime);
                    statement.setString(3, startTime);

                    try (java.sql.ResultSet result = statement.executeQuery()) {
                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalArgumentException(
                                    "Scheduling conflict");
                        }
                    }
                }

                // Check capacity while holding the write lock.
                int confirmedCount =
                        registrationDao.countConfirmed(connection, conferenceId);

                boolean full = confirmedCount >= capacity;
                String status = full ? "WAITLISTED" : "CONFIRMED";

                int registrationId = registrationDao.createRegistration(
                        connection, userId, conferenceId, status);

                if (full) {
                    waitingListDao.addToWaitingList(connection, registrationId);
                }

                // Commit the transaction.
                try (Statement statement = connection.createStatement()) {
                    statement.execute("COMMIT");
                }

                return registrationId;

            } catch (SQLException | RuntimeException e) {

                try (Statement statement = connection.createStatement()) {
                    statement.execute("ROLLBACK");
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }












}