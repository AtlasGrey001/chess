package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection conn=DatabaseManager.getConnection();
             var stmt=conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username      VARCHAR(255) PRIMARY KEY,
                    password_hash VARCHAR(255) NOT NULL,
                    email         VARCHAR(255) NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    auth_token VARCHAR(255) PRIMARY KEY,
                    username   VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username)
                        ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    game_id        INT AUTO_INCREMENT PRIMARY KEY,
                    game_name      VARCHAR(255) NOT NULL,
                    white_username VARCHAR(255),
                    black_username VARCHAR(255),
                    game_over      BOOLEAN NOT NULL DEFAULT FALSE,
                    game_state     LONGTEXT NOT NULL,
                    FOREIGN KEY (white_username) REFERENCES users(username),
                    FOREIGN KEY (black_username) REFERENCES users(username)
                )
            """);

        } catch (SQLException ex) {
            throw new DataAccessException("Error initializing database",ex);}
    }
}
