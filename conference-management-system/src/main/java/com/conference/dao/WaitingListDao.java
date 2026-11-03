package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.model.WaitingList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WaitingListDao {
    public List<WaitingList> getWaitingList(int conferenceId)
            throws SQLException {

        List<WaitingList> waitingList = new ArrayList<>();

        String sql = """
            SELECT w.waiting_id, w.registration_id, w.joined_at
            FROM WaitingList w
            JOIN Registrations r
                ON w.registration_id = r.registration_id
            WHERE r.conference_id = ?
              AND r.status = 'WAITLISTED'
            ORDER BY w.waiting_id ASC
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    WaitingList entry = new WaitingList(
                            result.getInt("waiting_id"),
                            result.getInt("registration_id"),
                            result.getString("joined_at")
                    );

                    waitingList.add(entry);
                }
            }
        }

        return waitingList;
    }

    public int addToWaitingList(int registrationId) throws SQLException {

        String sql = """
            INSERT INTO WaitingList (registration_id)
            VALUES (?)
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            return statement.executeUpdate();
        }
    }

    public int removeFromWaitingList(int registrationId) throws SQLException {

        String sql = """
            DELETE FROM WaitingList
            WHERE registration_id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            return statement.executeUpdate();
        }
    }
}