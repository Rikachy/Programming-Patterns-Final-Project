
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

        try (Connection connection = ConnectionFactory.getConnection()) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA busy_timeout = 5000");
                statement.execute("BEGIN IMMEDIATE");
            }

            try {
                Registration registration =
                        registrationDao.findById(connection, registrationId);

                if (registration == null
                        || "CANCELLED".equals(registration.getStatus())) {

                    try (Statement statement = connection.createStatement()) {
                        statement.execute("ROLLBACK");
                    }
                    return false;
                }

                boolean wasConfirmed =
                        "CONFIRMED".equals(registration.getStatus());

                int conferenceId = registration.getConferenceId();

                registrationDao.updateRegistrationStatus(
                        connection, registrationId, "CANCELLED");

                waitingListDao.removeFromWaitingList(
                        connection, registrationId);

                if (wasConfirmed) {
                    List<WaitingList> waitingParticipants =
                            waitingListDao.getWaitingList(connection, conferenceId);

                    for (WaitingList entry : waitingParticipants) {

                        Registration candidate = registrationDao.findById(
                                connection, entry.getRegistrationId());

                        if (candidate == null
                                || !"WAITLISTED".equals(candidate.getStatus())) {
                            continue;
                        }

                        // Skip participants who have a scheduling conflict.
                        if (hasSchedulingConflict(
                                connection, candidate.getUserId(), conferenceId)) {
                            continue;
                        }

                        String eligibilitySql = """
                                SELECT COUNT(*)
                                FROM Users
                                WHERE user_id = ?
                                  AND role = 'PARTICIPANT'
                                """;

                        try (java.sql.PreparedStatement statement =
                                     connection.prepareStatement(eligibilitySql)) {

                            statement.setInt(1, candidate.getUserId());

                            try (java.sql.ResultSet result = statement.executeQuery()) {
                                if (!result.next() || result.getInt(1) == 0) {
                                    continue;
                                }
                            }
                        }

                        registrationDao.updateRegistrationStatus(
                                connection, candidate.getRegistrationId(), "CONFIRMED");

                        waitingListDao.removeFromWaitingList(
                                connection, candidate.getRegistrationId());

                        break;
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute("COMMIT");
                }

                return true;

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


                // Check for an existing registration.
                int cancelledRegistrationId = -1;

                String duplicateSql = """
                    SELECT registration_id, status
                    FROM Registrations
                    WHERE user_id = ?
                       AND conference_id = ?
                    """;

                try (java.sql.PreparedStatement statement =
                             connection.prepareStatement(duplicateSql)) {

                    statement.setInt(1, userId);
                    statement.setInt(2, conferenceId);

                    try (java.sql.ResultSet result = statement.executeQuery()) {

                        if (result.next()) {

                            if ("CANCELLED".equals(result.getString("status"))) {
                                cancelledRegistrationId =
                                        result.getInt("registration_id");
                            } else {
                                throw new IllegalArgumentException(
                                        "Already registered");
                            }
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


                int registrationId;

                if (cancelledRegistrationId != -1) {

                    registrationId = cancelledRegistrationId;

                    registrationDao.updateRegistrationStatus(
                            connection, registrationId, status);

                } else {

                    registrationId = registrationDao.createRegistration(
                            connection, userId, conferenceId, status);
                }


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

    private boolean hasSchedulingConflict(Connection connection,
                                          int userId,
                                          int conferenceId)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM Registrations r
            JOIN Conferences existing
              ON r.conference_id = existing.conference_id
            JOIN Conferences target
              ON target.conference_id = ?
            WHERE r.user_id = ?
              AND r.status = 'CONFIRMED'
              AND existing.start_time < target.end_time
              AND existing.end_time > target.start_time
            """;

        try (java.sql.PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);
            statement.setInt(2, userId);

            try (java.sql.ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

}