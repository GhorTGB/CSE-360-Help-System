package phase2;

import java.sql.*;

public class DatabaseHelper {

   
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/cse360database";

   
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;

    private static DatabaseHelper instance = null;

    private DatabaseHelper() {
        connectToDatabase();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void connectToDatabase() {
        try {
            Class.forName(JDBC_DRIVER); 
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            createTables();  
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement statement = connection.createStatement();

        String userTable = "CREATE TABLE IF NOT EXISTS users ("
                + "username VARCHAR(255) PRIMARY KEY, "
                + "password VARCHAR(255), "
                + "email VARCHAR(255), "
                + "firstName VARCHAR(255), "
                + "lastName VARCHAR(255))";
        statement.execute(userTable);

        String roleTable = "CREATE TABLE IF NOT EXISTS user_roles ("
                + "username VARCHAR(255), "
                + "role VARCHAR(20), "
                + "FOREIGN KEY (username) REFERENCES users(username))";
        statement.execute(roleTable);

        String articleTable = "CREATE TABLE IF NOT EXISTS articles ("
                + "id BIGINT PRIMARY KEY, "
                + "level VARCHAR(50), "
                + "title VARCHAR(255), "
                + "shortDescription VARCHAR(1024), "
                + "body TEXT, "
                + "sensitive BOOLEAN)";
        statement.execute(articleTable);

        String keywordTable = "CREATE TABLE IF NOT EXISTS article_keywords ("
                + "article_id BIGINT, "
                + "keyword VARCHAR(255), "
                + "FOREIGN KEY (article_id) REFERENCES articles(id))";
        statement.execute(keywordTable);

        String linkTable = "CREATE TABLE IF NOT EXISTS article_links ("
                + "article_id BIGINT, "
                + "link VARCHAR(255), "
                + "FOREIGN KEY (article_id) REFERENCES articles(id))";
        statement.execute(linkTable);

        String groupTable = "CREATE TABLE IF NOT EXISTS article_groups ("
                + "article_id BIGINT, "
                + "groupName VARCHAR(255), "
                + "FOREIGN KEY (article_id) REFERENCES articles(id))";
        statement.execute(groupTable);

        String invitationTable = "CREATE TABLE IF NOT EXISTS invitation_codes ("
                + "code VARCHAR(255) PRIMARY KEY, "
                + "role VARCHAR(20))";
        statement.execute(invitationTable);

        statement.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
