import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleEncryptionComparison {
    public static void main(String[] args) {
        // Database connection details
        String dbUrlWithoutEncryption = "jdbc:oracle:thin:@your_db_host:1521:your_service_name";
        String dbUrlWithEncryption = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=your_db_host)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=your_service_name)))";
        String dbUser = "your_db_username";
        String dbPassword = "your_db_password";

        // Connect without encryption
        System.out.println("Connecting without encryption:");
        testConnection(dbUrlWithoutEncryption, dbUser, dbPassword);

        // Connect with encryption
        System.out.println("\nConnecting with encryption:");
        testConnection(dbUrlWithEncryption, dbUser, dbPassword);
    }

    private static void testConnection(String dbUrl, String dbUser, String dbPassword) {
        Connection connection = null;

        try {
            // Load the Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish a database connection
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // Check if the connection is encrypted
            String isEncryptedQuery = "SELECT sys_context('USERENV', 'NETWORK_PROTOCOL') AS protocol FROM dual";
            try (PreparedStatement stmt = connection.prepareStatement(isEncryptedQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String protocol = rs.getString("protocol");
                    System.out.println("Network Protocol: " + protocol);
                }
            }

            // Execute a test query
            String testQuery = "SELECT 'Hello, Oracle Encryption Test!' FROM dual";
            try (PreparedStatement stmt = connection.prepareStatement(testQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(1);
                    System.out.println("Query Result: " + result);
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } finally {
            // Close the database connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}
