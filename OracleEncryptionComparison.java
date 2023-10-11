import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class OracleEncryptionComparison {
    public static void main(String[] args) {
        // Database connection details
        String dbHost = "your_db_host";
        int dbPort = 1521;
        String dbServiceName = "your_service_name";
        String dbUser = "your_db_username";
        String dbPassword = "your_db_password";

        // Connect without encryption
        System.out.println("Connecting without encryption:");
        testConnection(dbHost, dbPort, dbServiceName, dbUser, dbPassword, false);

        // Connect with encryption
        System.out.println("\nConnecting with encryption:");
        testConnection(dbHost, dbPort, dbServiceName, dbUser, dbPassword, true);
    }

    private static void testConnection(String dbHost, int dbPort, String dbServiceName, String dbUser, String dbPassword, boolean useSSL) {
        Connection connection = null;

        try {
            // Set up SSL/TLS properties
            Properties sslProperties = new Properties();
            if (useSSL) {
                sslProperties.setProperty("javax.net.ssl.trustStore", "/path/to/truststore.jks");
                sslProperties.setProperty("javax.net.ssl.trustStorePassword", "truststore_password");
                sslProperties.setProperty("javax.net.ssl.keyStore", "/path/to/keystore.jks");
                sslProperties.setProperty("javax.net.ssl.keyStorePassword", "keystore_password");
                // You may also specify other SSL/TLS properties like cipher suites, encryption levels, etc.
            }

            // Construct the JDBC URL
            String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=" + (useSSL ? "TCPS" : "TCP") + ")(HOST=" + dbHost + ")(PORT=" + dbPort + "))(CONNECT_DATA=(SERVICE_NAME=" + dbServiceName + ")))";

            // Load the Oracle JDBC driver with SSL/TLS properties
            DriverManager.getConnection(dbUrl, dbUser, dbPassword, sslProperties);

            // Check if the connection is encrypted
            String protocol = useSSL ? "TCPS" : "TCP";
            System.out.println("Network Protocol: " + protocol);

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
