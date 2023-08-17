import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.Subject;

public class ReconcileMetadata {

    private static final int THREAD_POOL_SIZE = 10;
    private static final String CSV_FILE_PATH = "C:\\Work\\prop_metadata_3.csv";
    private static final String INPUT_FILE_PATH = "C:\\Work\\guid_input.txt";
    private static final String CLASS_NAME = "P8_Test";
    private static final String OBJECT_STORE_NAME = " ";
    private static final String DOMAIN_NAME = " ";

    public static void main(String[] args) {

        String username = " ";
        String password = " ";
        String url = "https:// /wsi/FNCEWS40MTOM/";

        try {
            // Connect to the domain
            Connection connection = Factory.Connection.getConnection(url);
            Subject subject = UserContext.createSubject(connection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the domain
            Domain domain = Factory.Domain.fetchInstance(connection, DOMAIN_NAME, null);

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, OBJECT_STORE_NAME, null);

            // Read GUIDs from input file
            List<String> guids = readGuidsFromFile(INPUT_FILE_PATH);

            // Create thread pool for concurrent processing
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

/*            // Process each GUID
            for (String guid : guids) {
                executorService.submit(() -> processGUID(guid, objectStore));
            }*/
            
            // Process each GUID
            for (String guid : guids) {
                Subject threadSubject = UserContext.createSubject(connection, username, password, null);
                executorService.submit(() -> {
                    UserContext.get().pushSubject(threadSubject);
                    processGUID(guid, objectStore);
                    UserContext.get().popSubject();
                });
            }

            // Shutdown the thread pool
            executorService.shutdown();

            // Wait for all tasks to complete
            while (!executorService.isTerminated()) {
                // Wait for tasks to complete
            }

            // Print completion message
            System.out.println("Metadata extraction completed.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    /*private static void processGUID(String guid, ObjectStore objectStore) {
        try {
            // Fetch the document by GUID
            Document document = Factory.Document.fetchInstance(objectStore, guid, null);

            // Get the properties of the document
            Properties properties = document.getProperties();

            // Retrieve required metadata properties (add or modify as needed)
            String documentTitle = properties.getStringValue("DocumentTitle");
            String documentClass = document.getClassName();
           // String customProperty = properties.getStringValue("CustomProperty");

            // Write metadata to CSV file
            //writeMetadataToCSV(guid, documentTitle, documentClass, customProperty);
            writeMetadataToCSV(guid, documentTitle, documentClass);

            // Refresh the document
            document.refresh(new String[]{});

            // Print status message
            System.out.println("Metadata extracted for GUID: " + guid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    private static void processGUID(String guid, ObjectStore objectStore) {
        try {
            // Fetch the document by GUID
            Id docId = new Id(guid);
            Document document = Factory.Document.fetchInstance(objectStore, docId, null);

            // Get the properties of the document
            Properties properties = document.getProperties();

            // Retrieve required metadata properties (add or modify as needed)
            String documentTitle = properties.getStringValue("DocumentTitle");
            String documentClass = document.getClassName();
            //String customProperty = properties.getStringValue("CustomProperty");
            String lastModifier = properties.get("LastModifier").getStringValue() != null ? properties.get("LastModifier").getStringValue() : "N/A";
            String creator = properties.get("Creator").getStringValue() != null ? properties.get("Creator").getStringValue() : "N/A";
            
            
            String contentSize = properties.get("ContentSize").getStringValue() != null ? properties.get("ContentSize").getStringValue() : "N/A";
            // String dataLastModified = properties.get("DataLastModified").getStringValue() != null ? document.get_DateLastModified().toString() : "N/A";

            // Write metadata to CSV file
            //writeMetadataToCSV(guid, documentTitle, documentClass, customProperty);
            writeMetadataToCSV(guid, documentTitle, documentClass,lastModifier,creator,contentSize);

            // Refresh the document
            document.refresh(new String[]{});

            // Print status message
            System.out.println("Metadata extracted for GUID: " + guid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   // private static void writeMetadataToCSV(String guid, String documentTitle, String documentClass, String customProperty) {
    private static void writeMetadataToCSV(String guid, String documentTitle, String documentClass, String lastModifier, String creator, String contentSize) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE_PATH, true))) {
           // writer.println(guid + "," + documentTitle+ "," + documentClass + "," + customProperty);
            writer.println(guid + "," + documentTitle+ "," + documentClass+ "," + lastModifier+ "," + creator+ "," + contentSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readGuidsFromFile(String filePath) {
        List<String> guids = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                guids.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return guids;
    }
}

