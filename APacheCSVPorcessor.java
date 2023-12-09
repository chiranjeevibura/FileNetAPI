import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class CsvProcessor {

    private static final int NUM_PRODUCER_THREADS = 5;
    private static final int BATCH_SIZE = 10000;

    public static void main(String[] args) {
        String inputFilePath = "input.csv";
        String outputFilePath = "output.csv";

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_PRODUCER_THREADS + 1);

        try {
            processCsv(inputFilePath, outputFilePath, executorService);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private static void processCsv(String inputFilePath, String outputFilePath, ExecutorService executorService)
            throws IOException, InterruptedException {
        // Create a blocking queue for producer-consumer model
        BlockingQueue<CSVRecord> recordQueue = new LinkedBlockingQueue<>();

        // Start consumer thread for writing to output CSV
        executorService.submit(() -> {
            try {
                writeOutputCsv(outputFilePath, recordQueue, executorService);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Start producer threads for reading and processing CSV records
        for (int i = 0; i < NUM_PRODUCER_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    CSVParser csvParser = CSVFormat.DEFAULT.parse(new FileReader(inputFilePath));

                    int count = 0;
                    for (CSVRecord record : csvParser) {
                        recordQueue.offer(record);

                        if (++count % BATCH_SIZE == 0) {
                            Thread.sleep(1);
                        }
                    }

                    csvParser.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        System.out.println("Processing completed successfully.");
    }

    private static void writeOutputCsv(String outputFilePath, BlockingQueue<CSVRecord> recordQueue, ExecutorService executorService)
            throws IOException {
        Set<String> uniqueAccounts = new HashSet<>();

        while (true) {
            CSVRecord record = recordQueue.poll();
            if (record != null) {
                String accountNumber = record.get(0).trim();

                processRow(accountNumber, uniqueAccounts);

                if (uniqueAccounts.size() >= BATCH_SIZE) {
                    try (FileWriter writer = new FileWriter(outputFilePath, true)) {
                        for (String uniqueAccount : uniqueAccounts) {
                            writer.write(uniqueAccount + "\n");
                        }
                    }

                    uniqueAccounts.clear();
                }
            } else {
                if (((ThreadPoolExecutor) executorService).getCompletedTaskCount() == NUM_PRODUCER_THREADS) {
                    break;
                }
            }
        }
    }

    private static void processRow(String accountNumber, Set<String> uniqueAccounts) {
        // Perform any processing if needed

        // Add the account number to the set
        uniqueAccounts.add(accountNumber);
    }
}
