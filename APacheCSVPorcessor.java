import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CsvProcessor {

    private static final Logger logger = Logger.getLogger(CsvProcessor.class.getName());
    private static final int NUM_PRODUCER_THREADS = 5;
    private static final int BATCH_SIZE = 10000;

    public static void main(String[] args) {
        String inputFilePath = "input.csv";
        String outputFilePath = "output.csv";

        try {
            processCsv(inputFilePath, outputFilePath);
        } catch (IOException | InterruptedException e) {
            logger.severe("Error during CSV processing: " + e.getMessage());
        }
    }

    private static void processCsv(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        configureLogger();  // Configuring logger

        // Create a blocking queue for producer-consumer model
        BlockingQueue<CSVRecord> recordQueue = new LinkedBlockingQueue<>();

        // Create executor service with producer and consumer threads
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PRODUCER_THREADS + 1);

        // Start consumer thread for writing to output CSV
        executorService.submit(() -> {
            try {
                writeOutputCsv(outputFilePath, recordQueue);
            } catch (IOException e) {
                logger.severe("Error during writing output CSV: " + e.getMessage());
            }
        });

        // Start producer threads for reading and processing CSV records
        for (int i = 0; i < NUM_PRODUCER_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    // Create a CSVParser with streaming capabilities
                    CSVParser csvParser = CSVFormat.DEFAULT.parse(new FileReader(inputFilePath));

                    // Process CSV records in batches
                    int count = 0;
                    for (CSVRecord record : csvParser) {
                        // Offer the record to the queue
                        recordQueue.offer(record);

                        // Batch processing: Check if it's time to write to output
                        if (++count % BATCH_SIZE == 0) {
                            Thread.sleep(1); // Introduce a short delay to allow other threads to catch up
                        }
                    }

                    // Close the CSVParser
                    csvParser.close();
                } catch (IOException | InterruptedException e) {
                    logger.severe("Error during CSV processing: " + e.getMessage());
                }
            });
        }

        // Wait for all threads to complete
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);

        logger.info("Processing completed successfully.");
    }

    private static void writeOutputCsv(String outputFilePath, BlockingQueue<CSVRecord> recordQueue) throws IOException {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            // Set to store unique account numbers
            Set<String> uniqueAccounts = new HashSet<>();

            // Continuously poll records from the queue
            while (true) {
                CSVRecord record = recordQueue.poll();
                if (record != null) {
                    String accountNumber = record.get(0).trim();

                    // Process the account number
                    processRow(accountNumber, uniqueAccounts);

                    // Write unique account numbers to the output CSV
                    if (uniqueAccounts.size() >= BATCH_SIZE) {
                        for (String uniqueAccount : uniqueAccounts) {
                            writer.write(uniqueAccount + "\n");
                        }
                        // Clear the set after writing
                        uniqueAccounts.clear();
                    }
                }
            }
        }
    }

    private static void processRow(String accountNumber, Set<String> uniqueAccounts) {
        // Perform any processing if needed

        // Add the account number to the set
        uniqueAccounts.add(accountNumber);
    }

    private static void configureLogger() throws IOException {
        FileHandler fileHandler = new FileHandler("CsvProcessor.log");
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);  // Disable console logging
    }
}
