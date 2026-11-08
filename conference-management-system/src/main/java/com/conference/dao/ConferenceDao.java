package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.exception.DaoException;
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

    public int createConference(Conference conference) {

        String sql = """
                INSERT INTO Conferences
                    (title, description, location, start_time, end_time, capacity)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING conference_id
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conference.getTitle());
            statement.setString(2, conference.getDescription());
            statement.setString(3, conference.getLocation());
            statement.setString(4, conference.getStartTime());
            statement.setString(5, conference.getEndTime());
            statement.setInt(6, conference.getCapacity());

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt("conference_id");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "conference creation failed", e);
            throw new DaoException("cannot create conference", e);
        }
    }

    public Conference findById(int conferenceId) {

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
                    "ID does not exist : " + conferenceId, e);
            throw new DaoException(
                    "conference is not findable :( " + conferenceId, e);
        }
    }

    public List<Conference> findAll() {

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
            LOGGER.log(Level.SEVERE, "failed to retrieve conferences :(", e);
            throw new DaoException("couldn't retrive the conference", e);
        }

        return conferences;
    }

    public int updateConference(Conference conference) {
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
                    "can't update conference :(  " + conference.getConferenceId(), e);
            throw new DaoException(
                    "could not update conference " + conference.getConferenceId(), e);
        }
    }

    // DELETE: returns the number of rows deleted (0 = conference not found).
    public int deleteConference(int conferenceId) {

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
                    "can't delete conference :( " + conferenceId, e);
            throw new DaoException(
                    "could not delete conference " + conferenceId, e);
        }
    }
}