import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.text.SimpleDateFormat;

public class GenerateAndInsertData {

    // Database connection parameters
    private static final String DB_URL = "jdbc:oracle:thin:@your_oracle_db_host:1521:your_oracle_db_service";
    private static final String DB_USER = "your_db_user";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        int numberOfAccounts = 10; // Adjust the number of accounts as needed
        int numberOfRowsInTarget = 20; // Adjust the number of rows in target table

        List<Map<String, String>> sourceTable = generateSourceTableData(numberOfAccounts);
        List<Map<String, String>> targetTable = generateTargetTableData(sourceTable, numberOfRowsInTarget);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            insertSourceTableData(conn, sourceTable);
            insertTargetTableData(conn, targetTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Data has been inserted into the database.");
    }

    private static List<Map<String, String>> generateSourceTableData(int count) {
        List<Map<String, String>> table = new ArrayList<>();
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < count; i++) {
            Map<String, String> row = new HashMap<>();
            String accountNumber = generateAccountNumber(random);

            // Generating a random date
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -random.nextInt(365));
            String closeDate = dateFormat.format(cal.getTime());

            row.put("account_number", accountNumber);
            row.put("close_date", closeDate);

            table.add(row);
        }

        return table;
    }

    private static List<Map<String, String>> generateTargetTableData(List<Map<String, String>> sourceTable, int count) {
        List<Map<String, String>> table = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            Map<String, String> row = new HashMap<>();
            String accountNumber = sourceTable.get(random.nextInt(sourceTable.size())).get("account_number");

            row.put("account_number", accountNumber);
            row.put("uuid", UUID.randomUUID().toString()); // Unique UUID for each row
            row.put("column_1", generateRandomString(random, 6));
            row.put("column_2", generateRandomString(random, 6));
            row.put("close_date", ""); // Empty initially
            row.put("record_code", ""); // Empty initially

            table.add(row);
        }

        return table;
    }

    private static void insertSourceTableData(Connection conn, List<Map<String, String>> sourceTable) throws SQLException {
        String insertSourceSQL = "INSERT INTO spark_source_table (account_number, close_date) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSourceSQL)) {
            for (Map<String, String> row : sourceTable) {
                pstmt.setString(1, row.get("account_number"));
                pstmt.setString(2, row.get("close_date"));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static void insertTargetTableData(Connection conn, List<Map<String, String>> targetTable) throws SQLException {
        String insertTargetSQL = "INSERT INTO spark_target_table (account_number, uuid, column_1, column_2, close_date, record_code) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertTargetSQL)) {
            for (Map<String, String> row : targetTable) {
                pstmt.setString(1, row.get("account_number"));
                pstmt.setString(2, row.get("uuid"));
                pstmt.setString(3, row.get("column_1"));
                pstmt.setString(4, row.get("column_2"));
                pstmt.setString(5, row.get("close_date"));
                pstmt.setString(6, row.get("record_code"));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private static String generateAccountNumber(Random random) {
        StringBuilder sb = new StringBuilder();
        sb.append(random.nextBoolean() ? '4' : '5'); // Start with 4 or 5
        for (int i = 1; i < 16; i++) {
            sb.append(random.nextInt(10)); // Add remaining 15 digits
        }
        return sb.toString();
    }

    private static String generateRandomString(Random random, int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
___________
  CREATE TABLE spark_source_table (
    account_number VARCHAR2(16),
    close_date DATE
);
___________
  CREATE TABLE spark_target_table (
    account_number VARCHAR2(16),
    uuid VARCHAR2(36),
    column_1 VARCHAR2(10),
    column_2 VARCHAR2(10),
    close_date DATE,
    record_code VARCHAR2(10)
);
