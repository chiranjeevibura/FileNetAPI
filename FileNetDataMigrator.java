import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.util.UserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileNetDataMigrator {

    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        String uri = " ";
        String username = "your_username";
        String password = "your_password";
        String osName = "your_os_name";
        String sourceStorageAreaGuid = "source_storage_area_guid";
        String targetStorageAreaGuid = "target_storage_area_guid";

        try {
        	
        	// Connect to the source domain
			Connection connection = Factory.Connection.getConnection(uri);
			UserContext sourceUserContext = UserContext.get();
			sourceUserContext.pushSubject(
					UserContext.createSubject(connection, username, password, "FileNetP8WSI"));

			// Get the source domain
			Domain domain = Factory.Domain.fetchInstance(connection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + domain.get_Name());

			// Get the source object store
			ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, osName, null);
			System.out.println("Source Object Store: " + objectStore.get_DisplayName());
            

            Folder rootFolder = objectStore.get_RootFolder();
            Folder sourceStorageArea = Factory.Folder.fetchInstance(objectStore, sourceStorageAreaGuid, null);
            Folder targetStorageArea = Factory.Folder.fetchInstance(objectStore, targetStorageAreaGuid, null);

            List<Document> documents = getDocumentsInFolder(sourceStorageArea);
            int numDocumentsBeforeMigration = documents.size();
            System.out.println("Number of documents before migration: " + numDocumentsBeforeMigration);

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (Document document : documents) {
                executorService.submit(() -> migrateDocument(document, targetStorageArea));
            }

            executorService.shutdown();

            // Wait for all tasks to complete
            while (!executorService.isTerminated()) {
                // Do nothing, just wait
            }

            documents = getDocumentsInFolder(targetStorageArea);
            int numDocumentsAfterMigration = documents.size();
            System.out.println("Number of documents after migration: " + numDocumentsAfterMigration);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();

        }
    }

    private static List<Document> getDocumentsInFolder(Folder folder) {
        List<Document> documents = new ArrayList<>();
        for (ReferentialContainmentRelationship rel : folder.get_Containees()) {
            if (rel.get_Containee() instanceof Document) {
                documents.add((Document) rel.get_Containee());
            }
        }
        return documents;
    }

    private static void migrateDocument(Document document, Folder targetStorageArea) {
        ContentElementList contentElements = document.get_ContentElements();
        if (contentElements != null && contentElements.size() > 0) {
            ContentTransfer contentTransfer = (ContentTransfer) contentElements.get(0);
            Properties properties = document.getProperties();
            Property storageAreaProperty = properties.get("StorageArea");
            if (storageAreaProperty != null) {
                storageAreaProperty.setObjectValue(targetStorageArea.get_Id().toString());
                document.save(RefreshMode.NO_REFRESH);
            }
        }
    }
}




