package com.conference.dao;

import com.conference.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.conference.model.Registration;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDao {

    public int countConfirmedRegistrations(int conferenceId)
            throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM Registrations
                WHERE conference_id = ?
                AND status = 'CONFIRMED'
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public boolean isAlreadyRegistered(int userId, int conferenceId)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM Registrations
            WHERE user_id = ?
              AND conference_id = ?
              AND status != 'CANCELLED'
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, conferenceId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    public Registration findById(int registrationId) throws SQLException {

        String sql = """
            SELECT registration_id, user_id, conference_id,
                   status, registered_at
            FROM Registrations
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new Registration(
                            result.getInt("registration_id"),
                            result.getInt("user_id"),
                            result.getInt("conference_id"),
                            result.getString("status"),
                            result.getString("registered_at")
                    );
                }

                return null;
            }
        }
    }

    public List<Registration> findAll() throws SQLException {

        List<Registration> registrations = new ArrayList<>();

        String sql = """
            SELECT registration_id, user_id, conference_id,
                   status, registered_at
            FROM Registrations
            ORDER BY registration_id
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Registration registration = new Registration(
                        result.getInt("registration_id"),
                        result.getInt("user_id"),
                        result.getInt("conference_id"),
                        result.getString("status"),
                        result.getString("registered_at")
                );

                registrations.add(registration);
            }
        }

        return registrations;
    }

    public int createRegistration(int userId, int conferenceId, String status)
            throws SQLException {

        String sql = """
            INSERT INTO Registrations (user_id, conference_id, status)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, conferenceId);
            statement.setString(3, status);

            return statement.executeUpdate();
        }
    }

    public int updateRegistrationStatus(int registrationId, String newStatus)
            throws SQLException {

        if (!newStatus.equals("CONFIRMED")
                && !newStatus.equals("WAITLISTED")
                && !newStatus.equals("CANCELLED")) {
            throw new IllegalArgumentException("Invalid registration status");
        }

        String sql = """
            UPDATE Registrations
            SET status = ?
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus);
            statement.setInt(2, registrationId);

            return statement.executeUpdate();
        }
    }

    public int deleteRegistration(int registrationId)
            throws SQLException {

        String sql = """
            DELETE FROM Registrations
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            return statement.executeUpdate();
        }
    }
}