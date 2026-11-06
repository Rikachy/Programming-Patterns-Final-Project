package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDao {

    private static final Logger LOGGER =
            Logger.getLogger(UserDao.class.getName());

    // Turns the current row of a ResultSet into a User object.
    private User readUser(ResultSet result) throws SQLException {
        return new User(
                result.getInt("user_id"),
                result.getString("full_name"),
                result.getString("email"),
                result.getInt("age"),
                result.getString("role"),
                result.getString("created_at")
        );
    }

    // CREATE: returns the new user_id. created_at uses the table default.
    public int createUser(User user) throws SQLException {

        String sql = """
                INSERT INTO Users (full_name, email, age, role)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setInt(3, user.getAge());
            statement.setString(4, user.getRole());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("User created, but no ID was returned");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create user", e);
            throw e;
        }
    }

    // READ one user by ID. Returns null if not found.
    public User findById(int userId) throws SQLException {

        String sql = """
                SELECT user_id, full_name, email, age, role, created_at
                FROM Users
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readUser(result);
                }
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by ID: " + userId, e);
            throw e;
        }
    }

    // READ one user by email (used for login). Returns null if not found.
    public User findByEmail(String email) throws SQLException {

        String sql = """
                SELECT user_id, full_name, email, age, role, created_at
                FROM Users
                WHERE email = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readUser(result);
                }
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by email", e);
            throw e;
        }
    }

    // READ all users
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT user_id, full_name, email, age, role, created_at
                FROM Users
                ORDER BY user_id
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(readUser(result));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve users", e);
            throw e;
        }

        return users;
    }

    // UPDATE: returns the number of rows changed (0 = user not found).
    public int updateUser(User user) throws SQLException {

        String sql = """
                UPDATE Users
                SET full_name = ?, email = ?, age = ?, role = ?
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setInt(3, user.getAge());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getUserId());

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to update user: " + user.getUserId(), e);
            throw e;
        }
    }

    // DELETE: returns the number of rows deleted (0 = user not found).
    public int deleteUser(int userId) throws SQLException {

        String sql = """
                DELETE FROM Users
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete user: " + userId, e);
            throw e;
        }
    }
}