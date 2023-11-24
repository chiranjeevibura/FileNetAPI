import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class LineMergerWorker implements Runnable {
    private final BlockingQueue<String> inputQueue;
    private final BufferedWriter outputWriter;

    public LineMergerWorker(BlockingQueue<String> inputQueue, BufferedWriter outputWriter) {
        this.inputQueue = inputQueue;
        this.outputWriter = outputWriter;
    }

    @Override
    public void run() {
        try {
            String mergedLine = "";
            String prevLine = null;

            while (true) {
                String line = inputQueue.take();

                if (line.equals("EOF")) {
                    break;
                }

                if (line.startsWith("|")) {
                    if (prevLine != null) {
                        int countPrev = countOccurrences(prevLine, '|');
                        int countCurrent = countOccurrences(line, '|');

                        // Merge lines if both have less than 4 '|' characters
                        if (countPrev < 4 && countCurrent < 4) {
                            mergedLine = prevLine + line;
                            prevLine = mergedLine;
                            continue;
                        }

                        outputWriter.write(prevLine);
                        outputWriter.newLine();
                    }
                    prevLine = line;
                } else {
                    if (prevLine != null) {
                        prevLine += line;
                    }
                }
            }

            // Append the last line if it's not merged
            if (prevLine != null) {
                outputWriter.write(prevLine);
            }

            outputWriter.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int countOccurrences(String str, char target) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == target) {
                count++;
            }
        }
        return count;
    }
}

public class MultiThreadedLineMerger {

    public static void main(String[] args) {
        String inputFilePath = "input.txt";
        String outputFilePath = "output.txt";
        int numThreads = 4; // Adjust the number of threads as needed

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
            Thread[] workerThreads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                workerThreads[i] = new Thread(new LineMergerWorker(inputQueue, writer));
                workerThreads[i].start();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                inputQueue.put(line);
            }

            // Send EOF signal to workers
            for (int i = 0; i < numThreads; i++) {
                inputQueue.put("EOF");
            }

            // Wait for worker threads to finish
            for (Thread workerThread : workerThreads) {
                workerThread.join();
            }

            reader.close();
            writer.close();
            System.out.println("Merging completed successfully with multi-threading.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
