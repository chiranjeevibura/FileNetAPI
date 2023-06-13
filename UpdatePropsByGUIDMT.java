import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import javax.security.auth.Subject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdatePropsByGUIDMT {

    public static void main(String[] args) {

		String username = " ";
		String password = " ";
		String url = "https:// /wsi/FNCEWS40MTOM/";
		String className = " ";
		String objectStoreName = " ";
		int numThreads = 1;

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

           // Create a search to retrieve document objects of the given class
            String query = "SELECT * FROM " + className;
            SearchSQL searchSQL = new SearchSQL(query);
            SearchScope searchScope = new SearchScope(objectStore);

            // Fetch the document objects using the search
            RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, false);
            
           // System.out.println("Number of documents to be updated: " + countDoc);

            long startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            // Read input CSV file
            String csvFile = "C:\\Work\\input.csv";
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            // Create output CSV file
            String outputCsvFile = "C:\\Work\\output.csv";
            FileWriter writer = new FileWriter(outputCsvFile);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                // Get GUID and value from CSV columns
                String guid = parts[0].trim();
                String value = parts[1].trim();

                // Find the document with the matching GUID
                RepositoryRow matchingRow = null;
                Iterator<?> iterator = rowSet.iterator();
                while (iterator.hasNext()) {
                    RepositoryRow row = (RepositoryRow) iterator.next();
                    Properties properties = row.getProperties();
                    Id documentId = properties.getIdValue("Id");
                    String rowGuid = documentId.toString();
                    if (rowGuid.equals(guid)) {
                        matchingRow = row;
                        break;
                    }
                }

                if (matchingRow != null) {
                    // Update the specified property with the given value
                    Runnable updatePropsTask = updatePropTask(objectStore, matchingRow, value, writer);
                    executor.submit(updatePropsTask);
                } else {
                    System.out.println("Document not found for GUID: " + guid);
                }
            }

            br.close();

            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for tasks to complete
            }

            long endTime = System.currentTimeMillis();
            long updateTime = (endTime - startTime) / (1000 * 60);
            System.out.println("Update Time: " + updateTime + " minutes");
            System.out.println("Document objects of class " + className + " updated successfully.");

            // Close the output CSV file
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    private static Runnable updatePropTask(ObjectStore objectStore, RepositoryRow row, String value, FileWriter writer) {
        return () -> {
            try {
                System.out.println("Updating document: " + row.getProperties().get("GUID").getStringValue());

                // Update the specified property with the given value
                Properties properties = row.getProperties();
                properties.putValue("Fast2Import", value);
                Document document = (Document) row.getProperties().getObjectValue("This");
                document.save(RefreshMode.REFRESH);

                System.out.println("Updated document: " + row.getProperties().get("GUID").getStringValue());

                // Write update status, GUID, and properties to output CSV
                writer.append("Updated,");
                writer.append(row.getProperties().get("Id").getStringValue());
                writer.append(",");
                writer.append(value);
                writer.append("\n");

            } catch (Exception e) {
                System.out.println("Error updating document: " + row.getProperties().get("GUID").getStringValue());
                e.printStackTrace();
            }
        };
    }
}
