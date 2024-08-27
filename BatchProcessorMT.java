import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessorMT {
    private static String ORACLE_DB_URL;
    private static String ORACLE_DB_USERNAME;
    private static String ORACLE_DB_PASSWORD;
    private static String P8_SQL_QUERY;
    private static int BATCH_SIZE;

    public static void main(String[] args) {
        // Load properties from a file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            ORACLE_DB_URL = properties.getProperty("ORACLE_DB_URL");
            ORACLE_DB_USERNAME = properties.getProperty("ORACLE_DB_USERNAME");
            ORACLE_DB_PASSWORD = properties.getProperty("ORACLE_DB_PASSWORD");
            P8_SQL_QUERY = properties.getProperty("P8_SQL_QUERY");
            BATCH_SIZE = Integer.parseInt(properties.getProperty("BATCH_SIZE"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(ORACLE_DB_URL, ORACLE_DB_USERNAME, ORACLE_DB_PASSWORD)) {
            System.out.println("Connected to Oracle database successfully.");
            processBatches(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void processBatches(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM (" + P8_SQL_QUERY + ")";
        String dateRangeQuery = P8_SQL_QUERY + " ORDER BY createDate";
        String insertBatchQuery = "INSERT INTO BatchTable (batchID, FromDate, ToDate, batch_create_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement countStmt = connection.prepareStatement(countQuery);
             PreparedStatement dateRangeStmt = connection.prepareStatement(dateRangeQuery, 
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            // Count the total number of documents
            System.out.println("Executing count query: " + countQuery);
            ResultSet countResult = countStmt.executeQuery();
            int totalCount = 0;
            if (countResult.next()) {
                totalCount = countResult.getInt(1);
                System.out.println("Total document count: " + totalCount);
            } else {
                System.out.println("No documents found.");
                return;
            }

            // Calculate the number of batches
            int numBatches = (int) Math.ceil((double) totalCount / BATCH_SIZE);
            System.out.println("Total number of batches: " + numBatches);

            // ExecutorService to manage threads
            ExecutorService executorService = Executors.newFixedThreadPool(4); // Adjust the thread pool size as needed

            // Get the createDates
            System.out.println("Executing date range query: " + dateRangeQuery);
            ResultSet dateRangeResult = dateRangeStmt.executeQuery();
            
            // Move to the last row to get the total number of documents
            dateRangeResult.last();
            Timestamp[] createDates = new Timestamp[totalCount];
            int index = 0;
            dateRangeResult.beforeFirst(); // Move cursor back to the beginning
            
            while (dateRangeResult.next()) {
                createDates[index++] = dateRangeResult.getTimestamp("createDate");
                if (index % 10000 == 0) {
                    System.out.println("Processed " + index + " createDates...");
                }
            }
            System.out.println("Finished processing createDates.");

            // Insert batches into the BatchTable using multi-threading
            for (int i = 0; i < numBatches; i++) {
                final int batchIndex = i;
                executorService.submit(() -> {
                    try {
                        insertBatch(connection, createDates, batchIndex, insertBatchQuery);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            // Shut down the executor service and wait for tasks to finish
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // Wait for all threads to finish
            }
            System.out.println("All batches processed successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred during batch processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertBatch(Connection connection, Timestamp[] createDates, int batchIndex, String insertBatchQuery) throws SQLException {
        try (PreparedStatement insertBatchStmt = connection.prepareStatement(insertBatchQuery)) {
            String batchID = UUID.randomUUID().toString();
            Timestamp fromDate = createDates[batchIndex * BATCH_SIZE];
            Timestamp toDate = (batchIndex == createDates.length / BATCH_SIZE - 1) ? createDates[createDates.length - 1] : createDates[(batchIndex + 1) * BATCH_SIZE - 1];
            Timestamp batchCreateDate = Timestamp.from(Instant.now()); // Get current timestamp in UTC

            System.out.println("Inserting batch " + (batchIndex + 1) + " with batchID: " + batchID + ", FromDate: " + fromDate + ", ToDate: " + toDate + ", BatchCreateDate: " + batchCreateDate);
            insertBatchStmt.setString(1, batchID);
            insertBatchStmt.setTimestamp(2, fromDate);
            insertBatchStmt.setTimestamp(3, toDate);
            insertBatchStmt.setTimestamp(4, batchCreateDate);
            insertBatchStmt.executeUpdate();
            System.out.println("Batch " + (batchIndex + 1) + " inserted successfully.");
        }
    }
}
-------------
    import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessor {
    private static String ORACLE_DB_URL;
    private static String ORACLE_DB_USERNAME;
    private static String ORACLE_DB_PASSWORD;
    private static String P8_SQL_QUERY;
    private static String P8_PART_SQL_QUERY;
    private static int BATCH_SIZE;
    private static final int THREAD_POOL_SIZE = 4; // Define the number of threads for the thread pool

    public static void main(String[] args) {
        loadProperties();

        try (Connection connection = DriverManager.getConnection(ORACLE_DB_URL, ORACLE_DB_USERNAME, ORACLE_DB_PASSWORD)) {
            System.out.println("Connected to Oracle database successfully.");
            processBatches(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = BatchProcessor.class.getClassLoader().getResourceAsStream("DBDEV.properties")) {
            properties.load(input);

            ORACLE_DB_URL = properties.getProperty("ORACLE_DB_URL").trim();
            ORACLE_DB_USERNAME = properties.getProperty("ORACLE_DB_USERNAME").trim();
            ORACLE_DB_PASSWORD = properties.getProperty("ORACLE_DB_PASSWORD").trim();
            P8_SQL_QUERY = properties.getProperty("P8_SQL_QUERY").trim();
            P8_PART_SQL_QUERY = properties.getProperty("P8_PART_SQL_QUERY").trim();
            BATCH_SIZE = Integer.parseInt(properties.getProperty("BATCH_SIZE").trim());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processBatches(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM (" + P8_PART_SQL_QUERY + ")";
        String dateRangeQuery = P8_SQL_QUERY + " ORDER BY createDate";
        String insertBatchQuery = "INSERT INTO BatchTable (BATCHID, BATCH_START_DATE, BATCH_END_DATE, BATCH_CREATE_DATE) VALUES (?, ?, ?, ?)";

        try (PreparedStatement countStmt = connection.prepareStatement(countQuery);
             PreparedStatement dateRangeStmt = connection.prepareStatement(dateRangeQuery, 
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             PreparedStatement insertBatchStmt = connection.prepareStatement(insertBatchQuery)) {

            // Count the total number of documents
            System.out.println("Executing count query: " + countQuery);
            ResultSet countResult = countStmt.executeQuery();
            int totalCount = 0;
            if (countResult.next()) {
                totalCount = countResult.getInt(1);
                System.out.println("Total document count: " + totalCount);
            } else {
                System.out.println("No documents found.");
                return;
            }

            // Calculate the number of batches
            int numBatches = (int) Math.ceil((double) totalCount / BATCH_SIZE);
            System.out.println("Total number of batches: " + numBatches);

            // Get the createDates
            System.out.println("Executing date range query: " + dateRangeQuery);
            ResultSet dateRangeResult = dateRangeStmt.executeQuery();

            // Process and insert batches sequentially to ensure order
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            for (int i = 0; i < numBatches; i++) {
                final int batchIndex = i;
                executor.submit(() -> {
                    try {
                        dateRangeResult.absolute(batchIndex * BATCH_SIZE + 1);
                        Timestamp fromDate = dateRangeResult.getTimestamp("createDate");

                        Timestamp toDate;
                        if (batchIndex == numBatches - 1) {
                            dateRangeResult.last();
                            toDate = dateRangeResult.getTimestamp("createDate");
                        } else {
                            dateRangeResult.absolute((batchIndex + 1) * BATCH_SIZE);
                            toDate = dateRangeResult.getTimestamp("createDate");
                        }

                        String batchID = UUID.randomUUID().toString();
                        Timestamp batchCreateDate = new Timestamp(System.currentTimeMillis());

                        System.out.println("Inserting batch " + (batchIndex + 1) + " with batchID: " + batchID + 
                                           ", BATCH_START_DATE: " + fromDate + ", BATCH_END_DATE: " + toDate + ", BATCH_CREATE_DATE: " + batchCreateDate);
                        
                        synchronized (BatchProcessor.class) { // Synchronize to ensure ordered insertion
                            insertBatchStmt.setString(1, batchID);
                            insertBatchStmt.setTimestamp(2, fromDate);
                            insertBatchStmt.setTimestamp(3, toDate);
                            insertBatchStmt.setTimestamp(4, batchCreateDate);
                            insertBatchStmt.executeUpdate();
                            System.out.println("Batch " + (batchIndex + 1) + " inserted successfully.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to complete
            }
        } catch (SQLException e) {
            System.err.println("An error occurred during batch processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

