package com.conference.dao;

import com.conference.database.ConnectionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public class TestDatabase {


    public static void create() {
        try {
            ConnectionFactory.URL = "jdbc:sqlite:target/test.db";

            Files.deleteIfExists(Path.of("target", "test.db"));

            runSqlFile("database/schema.sql");
            runSqlFile("database/seed.sql");

        } catch (Exception e) {
            throw new RuntimeException("Could not build test database", e);
        }
    }
    private static void runSqlFile(String fileName) throws Exception {
        String text = Files.readString(Path.of(fileName));

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : text.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }
}