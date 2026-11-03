package com.conference.service;

import com.conference.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationService {
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
                }
                throw e;
            }
        }
    }
}