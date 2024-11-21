package phase3;

import java.sql.*;

public class DatabaseHelper { // Singleton class to manage database connections and operations.

    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() { // Private constructor to prevent direct instantiation.

        initializeDatabase();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeDatabase();
        }
        return connection;
    }
// Initializes the database by establishing a connection

    private void initializeDatabase() {
        try {
            // Establish connection to an in-memory H2 database

            connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(255) PRIMARY KEY, password VARCHAR(255))"); // Create users table to store user credentials

            stmt.execute("CREATE TABLE IF NOT EXISTS user_roles (username VARCHAR(255), role VARCHAR(50), PRIMARY KEY(username, role))");
            stmt.execute("CREATE TABLE IF NOT EXISTS articles (id BIGINT PRIMARY KEY, level VARCHAR(50), title VARCHAR(255), shortDescription VARCHAR(1024), body CLOB, sensitive BOOLEAN, author VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS article_keywords (article_id BIGINT, keyword VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS article_links (article_id BIGINT, link VARCHAR(1024))");
            stmt.execute("CREATE TABLE IF NOT EXISTS article_groups (article_id BIGINT, groupName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS group_users (groupName VARCHAR(255), username VARCHAR(255), accessLevel VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS special_access_groups (groupName VARCHAR(255) PRIMARY KEY)");
            stmt.execute("CREATE TABLE IF NOT EXISTS invitation_codes (code VARCHAR(255) PRIMARY KEY, role VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255), messageType VARCHAR(50), message CLOB)");
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    public void resetDatabase() { //Resets the database by dropping all existing objects and re-initializing the schema.

        try {
            if (connection != null && !connection.isClosed()) {
                Statement stmt = connection.createStatement(); // Drop all objects and delete files associated with the H2 database

                stmt.execute("DROP ALL OBJECTS DELETE FILES");
                stmt.close();
                connection.close();
            }
            connection = null;
            initializeDatabase();
            System.out.println("Database reset successfully.");
        } catch (SQLException e) {
            System.out.println("Error resetting database: " + e.getMessage());
        }
    }
}