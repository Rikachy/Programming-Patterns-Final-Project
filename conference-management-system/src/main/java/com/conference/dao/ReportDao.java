
package com.conference.dao;

import com.conference.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public List<String[]> getConferenceOverview()
            throws SQLException {

        String sql = """
                SELECT c.title,
                       c.capacity,
                       COUNT(r.registration_id) AS confirmed,
                       c.capacity - COUNT(r.registration_id) AS available
                FROM Conferences c
                LEFT JOIN Registrations r
                    ON c.conference_id = r.conference_id
                    AND r.status = 'CONFIRMED'
                GROUP BY c.conference_id, c.title, c.capacity
                ORDER BY c.conference_id
                """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                rows.add(new String[]{
                        result.getString("title"),
                        String.valueOf(result.getInt("capacity")),
                        String.valueOf(result.getInt("confirmed")),
                        String.valueOf(result.getInt("available"))
                });
            }
        }

        return rows;
    }


    public List<String[]> getConferenceParticipants(int conferenceId)
            throws SQLException {

        String sql = """
            SELECT u.full_name,
                   u.email,
                   u.age,
                   r.registered_at
            FROM Registrations r
            JOIN Users u ON r.user_id = u.user_id
            WHERE r.conference_id = ?
              AND r.status = 'CONFIRMED'
            ORDER BY r.registration_id
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    rows.add(new String[]{
                            result.getString("full_name"),
                            result.getString("email"),
                            String.valueOf(result.getInt("age")),
                            result.getString("registered_at")
                    });
                }
            }
        }

        return rows;
    }


    public List<String[]> getWaitingListReport(int conferenceId)
            throws SQLException {

        String sql = """
            SELECT ROW_NUMBER() OVER (
                       ORDER BY w.waiting_id
                   ) AS position,
                   u.full_name,
                   u.email,
                   w.joined_at
            FROM WaitingList w
            JOIN Registrations r
                ON w.registration_id = r.registration_id
            JOIN Users u
                ON r.user_id = u.user_id
            WHERE r.conference_id = ?
              AND r.status = 'WAITLISTED'
            ORDER BY w.waiting_id
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    rows.add(new String[]{
                            "#" + result.getInt("position"),
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("joined_at")
                    });
                }
            }
        }

        return rows;
    }


    public List<String[]> getParticipantRegistrations(int userId)
            throws SQLException {

        String sql = """
            SELECT c.title,
                   r.status,
                   c.start_time,
                   r.registered_at
            FROM Registrations r
            JOIN Conferences c
                ON r.conference_id = c.conference_id
            WHERE r.user_id = ?
              AND r.status IN ('CONFIRMED', 'WAITLISTED')
            ORDER BY r.registration_id
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    rows.add(new String[]{
                            result.getString("title"),
                            result.getString("status"),
                            result.getString("start_time"),
                            result.getString("registered_at")
                    });
                }
            }
        }

        return rows;
    }


    public List<String[]> getConferenceOccupancy()
            throws SQLException {

        String sql = """
            SELECT c.title,
                   c.capacity,
                   COUNT(r.registration_id) AS occupied,
                   CASE
                       WHEN c.capacity > 0
                       THEN COUNT(r.registration_id) * 100.0 / c.capacity
                       ELSE 0.0
                   END AS occupancy
            FROM Conferences c
            LEFT JOIN Registrations r
                ON c.conference_id = r.conference_id
                AND r.status = 'CONFIRMED'
            GROUP BY c.conference_id, c.title, c.capacity
            ORDER BY c.conference_id
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                rows.add(new String[]{
                        result.getString("title"),
                        String.valueOf(result.getInt("capacity")),
                        String.valueOf(result.getInt("occupied")),
                        String.format(java.util.Locale.ROOT,
                                "%.1f%%", result.getDouble("occupancy"))
                });
            }
        }

        return rows;
    }


    public List<String[]> getUnregisteredConferences()
            throws SQLException {

        String sql = """
            SELECT c.title,
                   COALESCE(c.location, 'N/A') AS location,
                   c.start_time,
                   c.capacity
            FROM Conferences c
            WHERE NOT EXISTS (
                SELECT 1
                FROM Registrations r
                WHERE r.conference_id = c.conference_id
                  AND r.status IN ('CONFIRMED', 'WAITLISTED')
            )
            ORDER BY c.conference_id
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                rows.add(new String[]{
                        result.getString("title"),
                        result.getString("location"),
                        result.getString("start_time"),
                        String.valueOf(result.getInt("capacity"))
                });
            }
        }

        return rows;
    }






}
