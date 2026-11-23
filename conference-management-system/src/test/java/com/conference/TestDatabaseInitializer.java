
package com.conference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestDatabaseInitializer {

    public static void initialize() throws Exception {

        Path testDatabase = Path.of("conference-test.db");
        Files.deleteIfExists(testDatabase);
        Path projectFolder = Path.of("database");

        String schema = Files.readString(
                projectFolder.resolve("schema.sql"));

        String seed = Files.readString(
                projectFolder.resolve("seed.sql"));

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:conference-test.db")) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");

                for (String sql : schema.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }

                for (String sql : seed.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
            }
        }

        System.out.println("Test database initialized successfully!");
    }
}
