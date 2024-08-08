import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BatchProcessor {
    private static final String ORACLE_DB_URL = "jdbc:oracle:thin:@your_oracle_db_url";
    private static final String ORACLE_DB_USERNAME = "your_db_username";
    private static final String ORACLE_DB_PASSWORD = "your_db_password";
    private static final String P8_SQL_QUERY = "SELECT createDate FROM DocVersion WHERE your_conditions";

    private static final int BATCH_SIZE = 50000;

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(ORACLE_DB_URL, ORACLE_DB_USERNAME, ORACLE_DB_PASSWORD)) {
            processBatches(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void processBatches(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM (" + P8_SQL_QUERY + ")";
        String dateRangeQuery = P8_SQL_QUERY + " ORDER BY createDate";
        String insertBatchQuery = "INSERT INTO BatchTable (batchID, FromDate, ToDate) VALUES (?, ?, ?)";

        try (PreparedStatement countStmt = connection.prepareStatement(countQuery);
             PreparedStatement dateRangeStmt = connection.prepareStatement(dateRangeQuery);
             PreparedStatement insertBatchStmt = connection.prepareStatement(insertBatchQuery)) {

            // Count the total number of documents
            ResultSet countResult = countStmt.executeQuery();
            int totalCount = 0;
            if (countResult.next()) {
                totalCount = countResult.getInt(1);
            }

            // Calculate the number of batches
            int numBatches = (int) Math.ceil((double) totalCount / BATCH_SIZE);

            // Get the createDates
            ResultSet dateRangeResult = dateRangeStmt.executeQuery();
            dateRangeResult.last();
            java.sql.Date[] createDates = new java.sql.Date[totalCount];
            int index = 0;
            while (dateRangeResult.previous()) {
                createDates[index++] = dateRangeResult.getDate("createDate");
            }

            // Insert batches into the BatchTable
            for (int i = 0; i < numBatches; i++) {
                String batchID = UUID.randomUUID().toString();
                java.sql.Date fromDate = createDates[i * BATCH_SIZE];
                java.sql.Date toDate = (i == numBatches - 1) ? createDates[totalCount - 1] : createDates[(i + 1) * BATCH_SIZE - 1];

                insertBatchStmt.setString(1, batchID);
                insertBatchStmt.setDate(2, fromDate);
                insertBatchStmt.setDate(3, toDate);
                insertBatchStmt.executeUpdate();
            }
        }
    }
}
