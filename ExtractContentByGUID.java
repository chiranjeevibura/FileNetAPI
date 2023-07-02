import com.filenet.api.core.*;
import com.filenet.api.util.UserContext;
import javax.security.auth.Subject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExtractContentByGUID {

    public static void main(String[] args) {

        String username = " ";
        String password = " ";
        String url = " ";
        String objectStoreName = " ";
        String[] guids = {" "}; // Provide the GUIDs you want to extract content for

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

            // Extract content for each GUID
            for (String guid : guids) {
                Document document = Factory.Document.fetchInstance(objectStore, guid, null);

             // Get the content of the document
                InputStream inputStream = document.accessContentStream(0);
                byte[] content = readContentFromStream(inputStream);

                // Save the content to a file
                String contentName = document.getProperties().getStringValue("DocumentTitle");
                String fileName = guid + "." + contentName;
                saveContentToFile(content, fileName);

                System.out.println("Content extracted for GUID " + guid + " and saved as " + fileName);
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
}
