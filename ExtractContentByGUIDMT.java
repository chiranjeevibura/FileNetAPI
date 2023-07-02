import com.filenet.api.core.*;
import com.filenet.api.util.UserContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

public class ExtractContentByGUIDMT {

    public static void main(String[] args) {
        String username = " ";
        String password = " ";
        String url = " ";
        String objectStoreName = " ";
        String inputFile = "C:\\Work\\guids.txt"; // Path to the input text file containing GUIDs
        String outputFolder = "C:\\Work\\output\\"; // Output folder path

        try {
            // Connect to the source domain
            Connection sourceConnection = Factory.Connection.getConnection(url);
            Subject subject = UserContext.createSubject(sourceConnection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the source domain
            Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

            // Print the source domain connection status
            System.out.println("Source Domain connected: True");
            System.out.println("Source Domain name: " + sourceDomain.get_Name());

            // Get the source object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(sourceDomain, objectStoreName, null);
            System.out.println("Source Object Store: " + objectStore.get_DisplayName());

            // Read GUIDs from the input file
            List<String> guids = readGuidsFromFile(inputFile);

            // Extract content for each GUID using multiple threads
            int numThreads = Runtime.getRuntime().availableProcessors();
            System.out.println("Number of threads: " + numThreads);

            List<Thread> threads = new ArrayList<>();

            for (String guid : guids) {
                Thread thread = new Thread(() -> {
  

                    try {
                    	// Establish a user context for this thread
                    	UserContext.get().pushSubject(subject);
                        
                        Document document = Factory.Document.fetchInstance(objectStore, guid, null);

                        // Get the content of the document
                        InputStream inputStream = document.accessContentStream(0);
                        byte[] content = readContentFromStream(inputStream);

                        // Save the content to a file
                        String mimeType = document.get_MimeType();
                       // String fileName = guid + "." + mimeType;
                        String fileName = guid;
                        saveContentToFile(content, fileName);

                        System.out.println("Content extracted for GUID " + guid + " and saved as " + fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    private static byte[] readContentFromStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return outputStream.toByteArray();
    }

    private static void saveContentToFile(byte[] content, String fileName) throws IOException {
        OutputStream outputStream = new FileOutputStream(fileName);
        outputStream.write(content);
        outputStream.close();
    }

    private static List<String> readGuidsFromFile(String inputFile) {
        List<String> guids = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    guids.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return guids;
    }
}
