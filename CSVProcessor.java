import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CsvProcessor {

    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        String inputFilePath = "input.csv";
        String outputFilePath = "output.csv";

        processCsv(inputFilePath, outputFilePath);
    }

    private static void processCsv(String inputFilePath, String outputFilePath) {
        try {
            // Read CSV file
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));

            // Create thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            // Set to store unique account numbers across threads
            Set<String> uniqueAccounts = new HashSet<>();

            // Read CSV lines and submit tasks to thread pool
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length >= 2) {
                    String accountNumber = columns[0].trim();
                    String classValue = columns[1].trim();

                    executorService.submit(() -> processRow(accountNumber, classValue, uniqueAccounts));
                }
            }

            // Shutdown the thread pool and wait for termination
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Write unique account numbers to output CSV
            writeOutputCsv(outputFilePath, uniqueAccounts);

            // Close the reader
            reader.close();

            System.out.println("Processing completed successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processRow(String accountNumber, String classValue, Set<String> uniqueAccounts) {
        // Perform any processing if needed

        // Add the account number to the set
        uniqueAccounts.add(accountNumber);
    }

    private static void writeOutputCsv(String outputFilePath, Set<String> uniqueAccounts) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            // Write header if needed
            // writer.write("AccountNumber\n");

            // Write unique account numbers to the output CSV
            for (String accountNumber : uniqueAccounts) {
                writer.write(accountNumber + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
