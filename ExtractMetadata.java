import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.security.auth.Subject;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.filenet.api.core.*;
import com.filenet.api.admin.*;

public class ExtractMetadata {

    public static void main(String[] args) {
 
    	String uri = "https:// /wsi/FNCEWS40MTOM/";
		String username = " ";
		String password = " ";
		String objectStoreName = " ";
		String documentClassName = " ";
		String csvFilePath = "C:\\Work\\propdata.csv";

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

            // Build the search query to retrieve all documents of the specified class
            SearchSQL searchSQL = new SearchSQL();
            searchSQL.setQueryString("SELECT * FROM " + documentClassName);
            
            // Create the search scope
            SearchScope searchScope = new SearchScope(objectStore);

            // Execute the search and retrieve the document set
            RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, new Boolean(true));
            //DocumentSet documentSet = Factory.Document.fetchInstance(objectStore, null, null);

            // Create the CSV file and write the headers
            FileWriter csvWriter = new FileWriter(csvFilePath);
            //csvWriter.append("Document ID,Property 1,Property 2,...\n");
            csvWriter.append("Document ID,LastModifier,Creator,Fast2Import\n");


            // Iterate over each document and extract metadata
            Iterator<?> it = rowSet.iterator();
            while (it.hasNext()) {
                RepositoryRow row = (RepositoryRow) it.next();
                Document document = (Document) row.getProperties().getObjectValue("This");

                Properties properties = document.getProperties();

                // Write document ID
                csvWriter.append(document.get_Id().toString()).append(",");

               /* // Iterate over property definitions and add them to the CSV
                for (Iterator<?> propIt = properties.iterator(); propIt.hasNext();) {
                    Property property = (Property) propIt.next();
                    csvWriter.append(property.getPropertyName()).append(",");
                }*/
                // Retrieve the specific properties
      /*          String lastModifier = properties.get("LastModifier").getStringValue();
                String dataLastModified = properties.get("DataLastModified").getDateTimeValue().toString();
                String creator = properties.get("Creator").getStringValue();
                Boolean fast2Import = properties.get("Fast2Import").getBooleanValue();*/
                
                String lastModifier = properties.get("LastModifier").getStringValue() != null ? properties.get("LastModifier").getStringValue() : "N/A";
               // String dataLastModified = properties.get("DataLastModified").getStringValue() != null ? document.get_DateLastModified().toString() : "N/A";
                String creator = properties.get("Creator").getStringValue()  != null ? properties.get("Creator").getStringValue() : "N/A";
                String fast2Import = properties.get("Fast2Import").getStringValue()  != null ? properties.get("Fast2Import").getStringValue() : "N";


                // Write the properties to the CSV
                csvWriter.append(lastModifier).append(",").append(creator).append(",").append(fast2Import.toString()).append("\n");
                //csvWriter.append("\n");
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
}
