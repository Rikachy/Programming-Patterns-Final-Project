package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.model.Conference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceDao {

    private static final Logger LOGGER =
            Logger.getLogger(ConferenceDao.class.getName());

    // Turns the current row of a ResultSet into a Conference object.
    private Conference readConference(ResultSet result) throws SQLException {
        return new Conference(
                result.getInt("conference_id"),
                result.getString("title"),
                result.getString("description"),
                result.getString("location"),
                result.getString("start_time"),
                result.getString("end_time"),
                result.getInt("capacity"),
                result.getString("created_at")
        );
    }

    // CREATE: returns the new conference_id.
    // description and location can be null (they are optional).
    public int createConference(Conference conference) throws SQLException {

        String sql = """
                INSERT INTO Conferences
                    (title, description, location, start_time, end_time, capacity)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, conference.getTitle());
            statement.setString(2, conference.getDescription());
            statement.setString(3, conference.getLocation());
            statement.setString(4, conference.getStartTime());
            statement.setString(5, conference.getEndTime());
            statement.setInt(6, conference.getCapacity());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException(
                        "Conference created, but no ID was returned");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create conference", e);
            throw e;
        }
    }

    // READ one conference. Returns null if not found.
    public Conference findById(int conferenceId) throws SQLException {

        String sql = """
                SELECT conference_id, title, description, location,
                       start_time, end_time, capacity, created_at
                FROM Conferences
                WHERE conference_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readConference(result);
                }
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find conference: " + conferenceId, e);
            throw e;
        }
    }

    // READ all conferences, earliest first.
    public List<Conference> findAll() throws SQLException {

        List<Conference> conferences = new ArrayList<>();

        String sql = """
                SELECT conference_id, title, description, location,
                       start_time, end_time, capacity, created_at
                FROM Conferences
                ORDER BY start_time, conference_id
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                conferences.add(readConference(result));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve conferences", e);
            throw e;
        }

        return conferences;
    }

    // UPDATE: returns the number of rows changed (0 = conference not found).
    public int updateConference(Conference conference) throws SQLException {

        String sql = """
                UPDATE Conferences
                SET title = ?, description = ?, location = ?,
                    start_time = ?, end_time = ?, capacity = ?
                WHERE conference_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conference.getTitle());
            statement.setString(2, conference.getDescription());
            statement.setString(3, conference.getLocation());
            statement.setString(4, conference.getStartTime());
            statement.setString(5, conference.getEndTime());
            statement.setInt(6, conference.getCapacity());
            statement.setInt(7, conference.getConferenceId());

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to update conference: "
                            + conference.getConferenceId(), e);
            throw e;
        }
    }

    // DELETE: returns the number of rows deleted (0 = conference not found).
    public int deleteConference(int conferenceId) throws SQLException {

        String sql = """
                DELETE FROM Conferences
                WHERE conference_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to delete conference: " + conferenceId, e);
            throw e;
        }
    }
}