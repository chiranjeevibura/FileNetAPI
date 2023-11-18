import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PDFFileMover {

    public static void main(String[] args) {
        String manifestFilePath = "path/to/manifest.txt";
        String inputDirectory = "path/to/input";
        String outputDirectory = "path/to/output";
        int numberOfThreads = 5;

        movePDFFiles(manifestFilePath, inputDirectory, outputDirectory, numberOfThreads);
    }

    private static void movePDFFiles(String manifestFilePath, String inputDirectory, String outputDirectory,
                                     int numberOfThreads) {
        try (BufferedReader reader = new BufferedReader(new FileReader(manifestFilePath))) {
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\\|");
                if (columns.length >= 3) {
                    String pdfName = columns[2].trim();
                    if (!pdfName.isEmpty()) {
                        File sourceFile = new File(inputDirectory, pdfName);
                        File destinationFile = new File(outputDirectory, pdfName);

                        executorService.execute(() -> {
                            try {
                                Files.move(sourceFile.toPath(), destinationFile.toPath(),
                                        StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Moved: " + pdfName);
                            } catch (IOException e) {
                                System.err.println("Error moving file: " + pdfName);
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
