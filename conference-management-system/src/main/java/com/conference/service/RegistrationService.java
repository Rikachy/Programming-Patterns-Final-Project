
package com.conference.service;

import com.conference.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationService {

    private static final Logger LOGGER =
            Logger.getLogger(RegistrationService.class.getName());

    public void joinWaitingList(int registrationId) throws SQLException {

        String updateSql = """
            UPDATE Registrations
            SET status = 'WAITLISTED'
            WHERE registration_id = ?
              AND status = 'CONFIRMED'
            """;

        String insertSql = """
            INSERT INTO WaitingList (registration_id)
            VALUES (?)
            """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int updated;

                try (PreparedStatement update =
                             connection.prepareStatement(updateSql)) {

                    update.setInt(1, registrationId);
                    updated = update.executeUpdate();
                }

                if (updated != 1) {
                    throw new SQLException(
                            "Registration not found or not confirmed"
                    );
                }

                try (PreparedStatement insert =
                             connection.prepareStatement(insertSql)) {

                    insert.setInt(1, registrationId);
                    insert.executeUpdate();
                }

                connection.commit();

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    LOGGER.log(Level.SEVERE,
                            "Rollback failed while joining waiting list",
                            rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to join waiting list for registration "
                            + registrationId, e);
            throw e;
        }
    }

    public void cancelWaitingListRegistration(int registrationId)
            throws SQLException {

        String updateSql = """
            UPDATE Registrations
            SET status = 'CANCELLED'
            WHERE registration_id = ?
              AND status = 'WAITLISTED'
            """;

        String deleteSql = """
            DELETE FROM WaitingList
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int updated;

                try (PreparedStatement update =
                             connection.prepareStatement(updateSql)) {

                    update.setInt(1, registrationId);
                    updated = update.executeUpdate();
                }

                if (updated != 1) {
                    throw new SQLException(
                            "No active waiting-list registration found"
                    );
                }

                try (PreparedStatement delete =
                             connection.prepareStatement(deleteSql)) {

                    delete.setInt(1, registrationId);

                    if (delete.executeUpdate() != 1) {
                        throw new SQLException(
                                "Waiting-list entry not found"
                        );
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    LOGGER.log(Level.SEVERE,
                            "Rollback failed while cancelling waiting-list registration",
                            rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to cancel waiting-list registration "
                            + registrationId, e);
            throw e;
        }
    }

    public void cancelConfirmedRegistration(int registrationId)
            throws SQLException {

        String findConferenceSql = """
            SELECT conference_id
            FROM Registrations
            WHERE registration_id = ?
              AND status = 'CONFIRMED'
            """;

        String cancelSql = """
            UPDATE Registrations
            SET status = 'CANCELLED'
            WHERE registration_id = ?
              AND status = 'CONFIRMED'
            """;

        String findNextSql = """
            SELECT w.registration_id
            FROM WaitingList w
            JOIN Registrations r
                ON w.registration_id = r.registration_id
            WHERE r.conference_id = ?
              AND r.status = 'WAITLISTED'
            ORDER BY w.waiting_id ASC
            LIMIT 1
            """;

        String promoteSql = """
            UPDATE Registrations
            SET status = 'CONFIRMED'
            WHERE registration_id = ?
              AND status = 'WAITLISTED'
            """;

        String removeWaitingSql = """
            DELETE FROM WaitingList
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int conferenceId;

                try (PreparedStatement statement =
                             connection.prepareStatement(findConferenceSql)) {

                    statement.setInt(1, registrationId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Confirmed registration not found"
                            );
                        }

                        conferenceId = result.getInt("conference_id");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(cancelSql)) {

                    statement.setInt(1, registrationId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Cancellation failed");
                    }
                }

                Integer nextRegistrationId = null;

                try (PreparedStatement statement =
                             connection.prepareStatement(findNextSql)) {

                    statement.setInt(1, conferenceId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            nextRegistrationId =
                                    result.getInt("registration_id");
                        }
                    }
                }

                if (nextRegistrationId != null) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(promoteSql)) {

                        statement.setInt(1, nextRegistrationId);

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Promotion failed");
                        }
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement(removeWaitingSql)) {

                        statement.setInt(1, nextRegistrationId);

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not remove waiting-list entry"
                            );
                        }
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    LOGGER.log(Level.SEVERE,
                            "Rollback failed while cancelling confirmed registration",
                            rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to cancel confirmed registration "
                            + registrationId, e);
            throw e;
        }
    }
}
