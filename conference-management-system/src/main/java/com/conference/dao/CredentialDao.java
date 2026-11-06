package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.exception.DaoException;
import com.conference.model.Credential;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialDao {

    private static final Logger LOGGER =
            Logger.getLogger(CredentialDao.class.getName());

    // CREATE: the user must already exist (foreign key).
    public int createCredential(Credential credential) {

        String sql = """
                INSERT INTO Credentials (user_id, password_hash)
                VALUES (?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, credential.getUserId());
            statement.setString(2, credential.getPasswordHash());

            return statement.executeUpdate();

        } catch (SQLException e) {
            // We log the user ID but never the password hash.
            LOGGER.log(Level.SEVERE,
                    "Failed to create credential for user: "
                            + credential.getUserId(), e);
            throw new DaoException("Could not create credential", e);
        }
    }

    // READ: returns null if the user has no credential.
    public Credential findByUserId(int userId) {

        String sql = """
                SELECT user_id, password_hash
                FROM Credentials
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Credential(
                            result.getInt("user_id"),
                            result.getString("password_hash")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find credential for user: " + userId, e);
            throw new DaoException(
                    "Could not find credential for user " + userId, e);
        }
    }

    // UPDATE: changes the password hash of one user.
    public int updatePasswordHash(int userId, String newPasswordHash) {

        String sql = """
                UPDATE Credentials
                SET password_hash = ?
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newPasswordHash);
            statement.setInt(2, userId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to update password for user: " + userId, e);
            throw new DaoException(
                    "Could not update password for user " + userId, e);
        }
    }

    // DELETE
    public int deleteCredential(int userId) {

        String sql = """
                DELETE FROM Credentials
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to delete credential for user: " + userId, e);
            throw new DaoException(
                    "Could not delete credential for user " + userId, e);
        }
    }
}