package com.conference.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection connection = ConnectionFactory.getConnection()) {

            if (tableExists(connection, "Users")) {
                return;
            }

            runScript(connection, Path.of("database", "schema.sql"));
            runScript(connection, Path.of("database", "seed.sql"));

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Could not set up the database: " + e.getMessage(), e);
        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static void runScript(Connection connection, Path file) throws SQLException, IOException {
        String script = Files.readString(file);
        try (Statement statement = connection.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }
}