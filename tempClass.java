import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.util.UserContext;

public class FileNetAddFolderExample {
    public static void main(String[] args) {
        String username = "your-username";
        String password = "your-password";
        String url = "your-fileNet-url";
        String rootFolderName = "Root Folder";
        String folderName = "YourFolderName";
        String folderGUID = "YourFolderGUID";

        // Create a connection to the FileNet domain
        Connection connection = Factory.Connection.getConnection(url);
        try {
            // Establish the user context
            Subject subject = UserContext.createSubject(connection, username, password, null);
            UserContext.get().pushSubject(subject);

            // Get the default domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);

            // Print the domain connection status
            System.out.println("Domain connected: " + domain.isConnected());
            System.out.println("Domain name: " + domain.get_Name());

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, "your-objectstore-name", null);

            // Get the root folder
            Folder rootFolder = Factory.Folder.fetchInstance(objectStore, "/" + rootFolderName, null);

            // Create a new folder instance
            Folder folder = Factory.Folder.createInstance(objectStore, null);

            // Set the folder properties
            folder.getProperties().putValue("FolderName", folderName);
            folder.getProperties().putValue("FolderGUID", folderGUID);

            // Add the folder under the root folder
            rootFolder.add_SubFolder(folder);
            folder.save(RefreshMode.REFRESH);

            // Print the folder information
            System.out.println("Folder added: " + folder.get_Id().toString());
            System.out.println("Folder name: " + folder.getProperties().getStringValue("FolderName"));
            System.out.println("Folder GUID: " + folder.getProperties().getStringValue("FolderGUID"));
            System.out.println("Parent folder: " + rootFolder.get_PathName() + "/" + folderName);
            System.out.println();

            UserContext.get().popSubject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                Factory.Connection.release(connection);
            }
        }
    }
}