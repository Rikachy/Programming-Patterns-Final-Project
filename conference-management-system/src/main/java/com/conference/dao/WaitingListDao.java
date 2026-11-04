
package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.model.WaitingList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaitingListDao {

    private static final Logger LOGGER =
            Logger.getLogger(WaitingListDao.class.getName());

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

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to retrieve waiting list for conference " + conferenceId,
                    e);
            throw e;
        }

        return waitingList;
    }

    public int addToWaitingList(int registrationId)
            throws SQLException {

        String sql = """
                INSERT INTO WaitingList (registration_id)
                VALUES (?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to add registration to waiting list: " + registrationId,
                    e);
            throw e;
        }
    }

    public int removeFromWaitingList(int registrationId)
            throws SQLException {

        String sql = """
                DELETE FROM WaitingList
                WHERE registration_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to remove registration from waiting list: " + registrationId,
                    e);
            throw e;
        }
    }

    public WaitingList getNextWaitingParticipant(int conferenceId)
            throws SQLException {

        String sql = """
                SELECT w.waiting_id,
                       w.registration_id,
                       w.joined_at
                FROM WaitingList w
                JOIN Registrations r
                    ON w.registration_id = r.registration_id
                WHERE r.conference_id = ?
                  AND r.status = 'WAITLISTED'
                ORDER BY w.waiting_id ASC
                LIMIT 1
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, conferenceId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new WaitingList(
                            result.getInt("waiting_id"),
                            result.getInt("registration_id"),
                            result.getString("joined_at")
                    );
                }

                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to retrieve next waiting participant for conference "
                            + conferenceId,
                    e);
            throw e;
        }
    }

    public WaitingList findById(int waitingId)
            throws SQLException {

        String sql = """
                SELECT waiting_id, registration_id, joined_at
                FROM WaitingList
                WHERE waiting_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, waitingId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new WaitingList(
                            result.getInt("waiting_id"),
                            result.getInt("registration_id"),
                            result.getString("joined_at")
                    );
                }

                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find waiting-list entry: " + waitingId,
                    e);
            throw e;
        }
    }

    public int updateWaitingListEntry(int waitingId, int registrationId)
            throws SQLException {

        String sql = """
                UPDATE WaitingList
                SET registration_id = ?
                WHERE waiting_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, registrationId);
            statement.setInt(2, waitingId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to update waiting-list entry: " + waitingId,
                    e);
            throw e;
        }
    }
}
