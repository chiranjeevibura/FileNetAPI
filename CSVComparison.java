import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class CSVComparison {
    public static void main(String[] args) {
        String inputCsvFile1 = "C:\\Work\\input1.csv";
        String inputCsvFile2 = "C:\\Work\\input2.csv";
        String outputCsvFile = "C:\\Work\\output.csv";
        String columnToCompare = "loan_number"; // Change to the specific column name

        try {
            long startTime = System.currentTimeMillis();

            // Step 1: Read and sort both input CSV files in chunks
            List<String> sortedChunks1 = sortLargeCSV(inputCsvFile1, columnToCompare);
            List<String> sortedChunks2 = sortLargeCSV(inputCsvFile2, columnToCompare);

            // Step 2: Merge the sorted chunks to find unique records
            List<String> uniqueRecords = mergeSortedChunks(sortedChunks1, sortedChunks2, columnToCompare);

            // Step 3: Write the unique records to the output CSV file
            writeUniqueRecordsToCSV(uniqueRecords, outputCsvFile);

            long endTime = System.currentTimeMillis();
            System.out.println("Processing time: " + (endTime - startTime) + " milliseconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> sortLargeCSV(String inputFile, String columnToCompare) throws IOException {
        // Read the input CSV file in chunks and sort each chunk
        List<String> sortedChunks = new ArrayList<>();
        int chunkSize = 100000; // Adjust based on available memory

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String header = br.readLine();
            sortedChunks.add(header); // Include the header in the sorted chunks

            List<String> chunk = new ArrayList<>(chunkSize);
            String line;
            while ((line = br.readLine()) != null) {
                chunk.add(line);
                if (chunk.size() >= chunkSize) {
                    Collections.sort(chunk, new CSVComparator(header, columnToCompare));
                    sortedChunks.addAll(chunk);
                    chunk.clear();
                }
            }

            // Sort and add the remaining lines in the last chunk
            Collections.sort(chunk, new CSVComparator(header, columnToCompare));
            sortedChunks.addAll(chunk);
        }

        return sortedChunks;
    }

    private static List<String> mergeSortedChunks(List<String> sortedChunks1, List<String> sortedChunks2, String columnToCompare) {
        List<String> uniqueRecords = new ArrayList<>();
        Iterator<String> iterator1 = sortedChunks1.iterator();
        Iterator<String> iterator2 = sortedChunks2.iterator();

        String record1 = iterator1.hasNext() ? iterator1.next() : null;
        String record2 = iterator2.hasNext() ? iterator2.next() : null;

        while (record1 != null && record2 != null) {
            int compareResult = new CSVComparator(sortedChunks1.get(0), columnToCompare).compare(record1, record2);
            if (compareResult < 0) {
                uniqueRecords.add(record1);
                record1 = iterator1.hasNext() ? iterator1.next() : null;
            } else if (compareResult > 0) {
                uniqueRecords.add(record2);
                record2 = iterator2.hasNext() ? iterator2.next() : null;
            } else {
                // Records are equal, skip one
                record1 = iterator1.hasNext() ? iterator1.next() : null;
                record2 = iterator2.hasNext() ? iterator2.next() : null;
            }
        }

        // Add any remaining records
        while (record1 != null) {
            uniqueRecords.add(record1);
            record1 = iterator1.hasNext() ? iterator1.next() : null;
        }

        while (record2 != null) {
            uniqueRecords.add(record2);
            record2 = iterator2.hasNext() ? iterator2.next() : null;
        }

        return uniqueRecords;
    }

    private static void writeUniqueRecordsToCSV(List<String> uniqueRecords, String outputFile) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            for (String record : uniqueRecords) {
                bw.write(record);
                bw.newLine();
            }
        }
    }

    static class CSVComparator implements Comparator<String> {
        private final String header;
        private final String columnToCompare;

        public CSVComparator(String header, String columnToCompare) {
            this.header = header;
            this.columnToCompare = columnToCompare;
        }

        @Override
        public int compare(String record1, String record2) {
            String[] fields1 = record1.split(",");
            String[] fields2 = record2.split(",");

            int columnIndex = getColumnIndex(header.split(","), columnToCompare);

            return fields1[columnIndex].compareTo(fields2[columnIndex]);
        }

        private int getColumnIndex(String[] headers, String columnName) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals(columnName)) {
                    return i;
                }
            }
            return -1; // Column not found in header
        }
    }
}
