import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.*;
import com.filenet.api.util.UserContext;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;


import javax.security.auth.Subject;


import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteDocumentsByClassMultiThread3 {
	public static void main(String[] args) throws InterruptedException {
		String username = " ";
		String password = " ";
		String url = "https:// /wsi/FNCEWS40MTOM/";
	 
		String className = " ";
		String objectStoreName = " ";
		int numThreads = 10;

		// Create a connection to the FileNet domain

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

			// Get the count of documents before deletion
			// int countBeforeDeletion = rowSet.size();
			int countBeforeDeletion = 0;
			Iterator iterator = rowSet.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				countBeforeDeletion++;
			}
			System.out.println("Number of documents Before deletion: " + countBeforeDeletion);

			long startTime = System.currentTimeMillis();
			int countAfterDeletion;

			do {
				countAfterDeletion = 0;

				// Create a fixed thread pool with the specified number of
				// threads
				ExecutorService executor = Executors.newFixedThreadPool(numThreads);

				// Iterator over the document objects and delete them
				for (iterator = rowSet.iterator(); iterator.hasNext();) {
					RepositoryRow row = (RepositoryRow) iterator.next();
					Properties properties = row.getProperties();
					Id documentId = properties.getIdValue("Id");

					// Creation of a deletion task and submit it to the executor
					Runnable deletionTask = createDeletionTask(objectStore, documentId, subject);
					executor.submit(deletionTask);
				}

				// Shutdown the executor and wait for all tasks to complete
				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.SECONDS);

				// Perform a fresh search to get the updated count of documents
				// after deletion
				rowSet = searchScope.fetchRows(searchSQL, null, null, false);

				for (iterator = rowSet.iterator(); iterator.hasNext();) {
					iterator.next();
					countAfterDeletion++;
				}

				System.out.println("Number of documents After deletion: " + countAfterDeletion);
			} while (countAfterDeletion != 0);

			long endTime = System.currentTimeMillis();
			long purgeTime = (endTime - startTime) / (1000 * 60);
			System.out.println("Purge Time: " + purgeTime + " minutes");
			System.out.println("Document objects of class " + className + " deleted successfully.");
		} catch (EngineRuntimeException e) {
            System.out.println("Domain connected: false");
            e.printStackTrace();
        } finally {
            UserContext.get().popSubject();
        }
    }

	private static Runnable createDeletionTask(ObjectStore objectStore, Id documentId, Subject subject) {
		return () -> {
			try {
				UserContext.get().pushSubject(subject);
				System.out.println("Deleting document: " + documentId.toString());

				Document document = Factory.Document.fetchInstance(objectStore, documentId, null);
				document.delete();
				document.save(RefreshMode.REFRESH);

				System.out.println("Deleted document: " + documentId.toString());
			} catch (Exception e) {
				System.out.println("Error deleting document: " + documentId.toString());
				e.printStackTrace();
			} finally {
				UserContext.get().popSubject();
			}
		};
	}
}