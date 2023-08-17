import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.Subject;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.filenet.api.core.*;
import com.filenet.api.admin.*;

public class ExtractMetadataGUIDs {

    public static void main(String[] args) {

    	String uri = "https:// /wsi/FNCEWS40MTOM/";
		String username = " ";
		String password = " ";
		String objectStoreName = " ";
		String documentClassName = "P8_Test";
        String csvFilePath = "C:\\Work\\propdata_uat_1.csv";
        String inputFilePath = "C:\\Work\\guid_input.txt"; // Path to the input text file containing GUIDs

        try {
            // Connect to the source domain
            Connection sourceConnection = Factory.Connection.getConnection(uri);
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

            // Retrieve the document class
            ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(objectStore, documentClassName, null);

            // Create the CSV file and write the headers
            FileWriter csvWriter = new FileWriter(csvFilePath);
            csvWriter.append("Document ID,DocumentClass,LastModifier,Creator,Fast2Import,DocumentTitle,\n");

            // Read GUIDs from input file
            List<String> guids = readGuidsFromFile(inputFilePath);

            // Iterate over each GUID and extract metadata
            for (String guid : guids) {
                processGUID(guid, objectStore, csvWriter);
            }

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Metadata export completed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

    private static void processGUID(String guid, ObjectStore objectStore, FileWriter csvWriter) {
        try {
            // Build the search query to retrieve the document with the specified GUID
            SearchSQL searchSQL = new SearchSQL();
            searchSQL.setQueryString("SELECT * FROM P8_Test WHERE Id = '" + guid + "'");

            // Create the search scope
            SearchScope searchScope = new SearchScope(objectStore);

            // Execute the search and retrieve the document set
            RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, new Boolean(true));

            // Iterate over each document and extract metadata
            Iterator<?> it = rowSet.iterator();
            while (it.hasNext()) {
                RepositoryRow row = (RepositoryRow) it.next();
                Document document = (Document) row.getProperties().getObjectValue("This");

                Properties properties = document.getProperties();

                // Write document ID
                csvWriter.append(document.get_Id().toString()).append(",");

                String documentClass = document.getClassName();
                String lastModifier = properties.get("LastModifier").getStringValue() != null ? properties.get("LastModifier").getStringValue() : "N/A";
                String creator = properties.get("Creator").getStringValue() != null ? properties.get("Creator").getStringValue() : "N/A";
                String fast2Import = properties.get("Fast2Import").getStringValue() != null ? properties.get("Fast2Import").getStringValue() : "N";
                String documentTitle = properties.get("DocumentTitle").getStringValue() != null ? properties.get("DocumentTitle").getStringValue() : "N/A";
                int contentSize = properties.get("ContentSize").getInteger32Value() != null ? properties.get("ContentSize").getInteger32Value() : '0';
                
           
                // Write the properties to the CSV
                csvWriter.append(documentClass).append(",").append(lastModifier).append(",").append(creator).append(",").append(fast2Import).append(",").append(documentTitle).append(",").append(contentSize).append("\n");
            }
        } catch (Exception e) {
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
