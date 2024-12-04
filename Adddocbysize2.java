import com.filenet.api.core.*;
import com.filenet.api.constants.*;
import com.filenet.api.util.*;
import com.filenet.api.property.*;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.admin.ClassDefinition;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FileNetDocumentHandler {

    public static void main(String[] args) {
        try {
            // FileNet Connection Parameters
            String uri = "http://your_filenet_server/wsi/FNCEWS40MTOM/";
            String username = "your_username";
            String password = "your_password";
            String objectStoreName = "your_object_store";

            // Parameters to define file size and number of documents
            int fileSizeInMB = 5; // Size of the text file in MB
            int numberOfDocuments = 2; // Number of documents to add

            // Connect to FileNet
            Connection connection = Factory.Connection.getConnection(uri);
            Subject subject = UserContext.createSubject(connection, username, password, "FileNetP8");
            UserContext.get().pushSubject(subject);

            // Get ObjectStore
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

            // Add documents to FileNet and measure time
            for (int i = 0; i < numberOfDocuments; i++) {
                File txtFile = generateTextFile(fileSizeInMB, "Document_" + (i + 1));
                
                long startAddTime = System.nanoTime(); // Start timing
                String guid = addDocumentToP8(objectStore, txtFile, "Document_" + (i + 1));
                long endAddTime = System.nanoTime(); // End timing
                
                txtFile.delete(); // Cleanup temporary file

                // Log time taken to add document
                long timeTakenToAdd = TimeUnit.NANOSECONDS.toMillis(endAddTime - startAddTime);
                System.out.println("Time taken to add document " + (i + 1) + ": " + timeTakenToAdd + " ms");

                // Retrieve the document using GUID and measure time
                long startRetrieveTime = System.nanoTime(); // Start timing
                retrieveDocumentFromP8(objectStore, guid);
                long endRetrieveTime = System.nanoTime(); // End timing

                // Log time taken to retrieve document
                long timeTakenToRetrieve = TimeUnit.NANOSECONDS.toMillis(endRetrieveTime - startRetrieveTime);
                System.out.println("Time taken to retrieve document " + (i + 1) + ": " + timeTakenToRetrieve + " ms");
            }

            // Clean up the subject
            UserContext.get().popSubject();

            System.out.println("All operations completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generate a text file of specified size in MB
    private static File generateTextFile(int sizeInMB, String fileName) throws IOException {
        File file = new File(fileName + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            Random random = new Random();
            int totalCharacters = sizeInMB * 1024 * 1024;
            for (int i = 0; i < totalCharacters; i++) {
                writer.write((char) ('A' + random.nextInt(26))); // Write random characters
                if (i % 80 == 0) writer.newLine(); // Add a newline after 80 characters
            }
        }
        System.out.println("Generated file: " + file.getAbsolutePath() + " (" + sizeInMB + " MB)");
        return file;
    }

    // Add the generated text file to FileNet P8
    private static String addDocumentToP8(ObjectStore objectStore, File file, String documentTitle) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // Create ContentTransfer object
            ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
            contentTransfer.setCaptureSource(fileInputStream);
            contentTransfer.set_ContentType("text/plain");

            // Create ContentElementList and add ContentTransfer
            ContentElementList contentList = Factory.ContentElement.createList();
            contentList.add(contentTransfer);

            // Create a Document object
            Document document = Factory.Document.createInstance(objectStore, "Document");
            document.getProperties().putValue("DocumentTitle", documentTitle);
            document.set_ContentElements(contentList);
            document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
            document.save(RefreshMode.REFRESH);

            System.out.println("Added document to P8: " + document.get_Id());
            return document.get_Id().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieve a document from FileNet P8 using GUID
    private static void retrieveDocumentFromP8(ObjectStore objectStore, String guid) {
        try {
            // Fetch the document by GUID
            Document document = Factory.Document.fetchInstance(objectStore, new Id(guid), null);

            // Get the content
            ContentElementList contentList = document.get_ContentElements();
            for (Object element : contentList) {
                ContentTransfer contentTransfer = (ContentTransfer) element;
                InputStream inputStream = contentTransfer.accessContentStream();

                // Process the stream (e.g., write to a local file or just read the content)
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Retrieved document content of size: " + outputStream.size() + " bytes");
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
