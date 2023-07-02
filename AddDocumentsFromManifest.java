import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.util.UserContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.Subject;

public class AddDocumentsFromManifest {

    public static void main(String[] args) {
        String username = " ";
        String password = " ";
        String url = " ";
        String objectStoreName = " ";
        String manifestFile = "C:\\Work\\manifest.csv"; // Path to the manifest CSV file

        try {
            // Connect to the domain
            Connection connection = Factory.Connection.getConnection(url);
            Subject subject = UserContext.createSubject(connection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);

            // Print the domain connection status
            System.out.println("Domain connected: True");
            System.out.println("Domain name: " + domain.get_Name());

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

            // Print the object store details
            System.out.println("Object Store: " + objectStore.get_DisplayName());

            // Read the manifest file
            try (BufferedReader reader = new BufferedReader(new FileReader(manifestFile))) {
                String line;
                boolean headerRow = true;

                while ((line = reader.readLine()) != null) {
                    if (headerRow) {
                        headerRow = false;
                        continue;
                    }

                    String[] fields = line.split(",");
                    if (fields.length < 3) {
                        System.out.println("Invalid manifest entry: " + line);
                        continue;
                    }

                    String filePath = fields[0].trim();
                    String className = fields[1].trim();
                    String propertyName = fields[2].trim();
                    String propertyValue = fields[3].trim();

                    // Create the document
                    Document document = Factory.Document.createInstance(objectStore, className);

                    // Set properties
                    Properties properties = document.getProperties();
                    properties.putValue(propertyName, propertyValue);

                    // Set content
                    InputStream inputStream = new FileInputStream(filePath);
                    ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
                    contentTransfer.setCaptureSource(inputStream);
                    ContentElementList contentElementList = Factory.ContentElement.createList();
                    contentElementList.add(contentTransfer);
                    document.set_ContentElements(contentElementList);
                    document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION );
                    
                    // Save the document
                    document.save(RefreshMode.REFRESH);
                    
                    String documentId = document.get_Id().toString();
                    String documentName = document.get_Name().toString();
                  
                    // Print the document details
                    System.out.println("Document added: " + documentName);
                    System.out.println("Document ID: " + documentId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }
}
