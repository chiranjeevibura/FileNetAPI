import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.*;
import com.filenet.api.util.UserContext;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.Subject;

public class UpdatePropsByPropMT {

	public static void main(String[] args) {

		String username = " ";
		String password = " ";
		String url = " ";
		String className = " ";
		String objectStoreName = " ";
		int numThreads = 2;

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

			// Get the count of documents before updating
			int countDoc = 0;
			Iterator<?> iterator = rowSet.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				countDoc++;
			}

			System.out.println("Number of documents to be Updated: " + countDoc);
			//
			long startTime = System.currentTimeMillis();
 
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);

 
			for (Iterator<?> it = rowSet.iterator(); it.hasNext();) {
				RepositoryRow row = (RepositoryRow) it.next();
				Properties properties = row.getProperties();
				Id documentId = properties.getIdValue("Id");

				// Creation a update task and submit it to the executor
				Runnable updatepropsTask = updatePropTask(objectStore, documentId, subject);
				executor.submit(updatepropsTask);
			}

			// Shutdown the executor and wait for all tasks to complete
			executor.shutdown();

			while (!executor.isTerminated()) {
				// Wait for tasks to complete
			}

			long endTime = System.currentTimeMillis();
			long updateTime = (endTime - startTime) / (1000 * 60);

			System.out.println("Update Time: " + updateTime + " minutes");
			System.out.println("Document objects of class " + className + " udpated successfully.");
		} finally {
			UserContext.get().popSubject();

		}
	}

	private static Runnable updatePropTask(ObjectStore objectStore, Id documentId, Subject subject) {
		return () -> {
			try {
				UserContext.get().pushSubject(subject);
				System.out.println("Updating document: " + documentId.toString());

				Document document = Factory.Document.fetchInstance(objectStore, documentId, null);
				document.getProperties().putValue("Fast2Import", " ");
				document.save(RefreshMode.REFRESH);

				System.out.println("Updated document: " + documentId.toString());

			} catch (Exception e) {

				System.out.println("Error udpating document: " + documentId.toString());

				e.printStackTrace();
			} finally {
				UserContext.get().popSubject();
			}
		};
	}
}