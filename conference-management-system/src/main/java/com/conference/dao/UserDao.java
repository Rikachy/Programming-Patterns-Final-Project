package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.exception.DaoException;
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

    public int createUser(User user) {

        String sql = """
                INSERT INTO Users (full_name, email, age, role)
                VALUES (?, ?, ?, ?)
                RETURNING user_id
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setInt(3, user.getAge());
            statement.setString(4, user.getRole());

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt("user_id");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error while creating the user :(", e);
            throw new DaoException("cannot create user", e);
        }
    }

    public User findById(int userId) {

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
            LOGGER.log(Level.SEVERE, "incapable of finding user by their ID :( " + userId, e);
            throw new DaoException("cannot find the user " + userId, e);
        }
    }

    public User findByEmail(String email) {
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
            LOGGER.log(Level.SEVERE, "unable to find user by their email :( ", e);
            throw new DaoException("cannot not find user by email", e);
        }
    }

    public List<User> findAll() {

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
            LOGGER.log(Level.SEVERE, "unable to retrieve the users :( ", e);
            throw new DaoException("cannot retrieve users", e);
        }

        return users;
    }

    public int updateUser(User user) {

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
                    "unable to update user :( " + user.getUserId(), e);
            throw new DaoException(
                    "cannot update user " + user.getUserId(), e);
        }
    }

    public int deleteUser(int userId) {

        String sql = """
                DELETE FROM Users
                WHERE user_id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "unable to delete user :( " + userId, e);
            throw new DaoException("cannot delete user " + userId, e);
        }
    }
}