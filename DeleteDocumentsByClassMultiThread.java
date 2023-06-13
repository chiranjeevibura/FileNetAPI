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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit; 

public class DeleteDocumentsByClassMultiThread {
	//private static final int numThreads = 5;

	public static void main(String[] args) {

		String username = " ";
		String password = " ";
		String url = "https:// /wsi/FNCEWS40MTOM/";
		String className = "P8_Test";
		String objectStoreName = " ";
		int numThreads = 2;

		String certificatePath = "C:\\Work\\fnuatc.crt";

		// Create a connection to the FileNet domain
		try {
		// Load the SSL certificate
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		FileInputStream fileInputStream = new FileInputStream(certificatePath);
		X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
		fileInputStream.close();

		// Create a KeyStore and add the certificate
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setCertificateEntry("fileNetCertificate", certificate);

		// Create a TrustManager that trusts the certificate
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		// Create an SSL context with the TrustManager
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		Connection connection = Factory.Connection.getConnection(url);
		try {
			Subject subject = UserContext.createSubject(connection, username, password, null);

			UserContext.get().pushSubject(subject);

			// Get the default domain
			Domain domain = Factory.Domain.fetchInstance(connection, null, null);

			ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);

			// Print the domain connection status
			System.out.println("Domain connected: true");
			System.out.println("Domain name: " + domain.get_Name());

			// Create a search to retrieve document objects of the given class
			String query = "SELECT * FROM " + className;
			SearchSQL searchSQL = new SearchSQL(query);
			SearchScope searchScope = new SearchScope(objectStore);

			// Fetch the document objects using the search
			RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, false);

			// Get the count of documents before deletion
			int countBeforeDeletion = 0;
			Iterator iterator = rowSet.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				countBeforeDeletion++;
			}

			System.out.println("Number of documents Before deletion: " + countBeforeDeletion);
			//
			long startTime = System.currentTimeMillis();
			int countAfterDeletion = 1;
			
			while (countAfterDeletion != 0) { 
			// Create a fixed thread pool with the specified number of threads
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);

			// Iterator over the document objects and delete them
			// while (iterator.hasNext()) {
			for (Iterator it = rowSet.iterator(); it.hasNext();) {
				RepositoryRow row = (RepositoryRow) it.next();
				Properties properties = row.getProperties();
				Id documentId = properties.getIdValue("Id");

				// Creation a deletion task and submit it to the executor
				Runnable deletionTask = createDeletionTask(objectStore, documentId, subject);
				executor.submit(deletionTask);
			}
			
			// Shutdown the executor and wait for all tasks to complete
			executor.shutdown();

			while (!executor.isTerminated()) {
				// Wait for tasks to complete
			}
			
			// Perform a fresh search to get the updated count of documents
			// after deletion
			rowSet = searchScope.fetchRows(searchSQL, null, null, false);
			//int countAfterDeletion = 0;

			iterator = rowSet.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				countAfterDeletion++;
			}
		
			countAfterDeletion = 0;
			System.out.println("Number of documents After deletion: " + countAfterDeletion);
			
			}
			//Calculate the purge time
			//long purgeTime = System.currentTimeMillis() - startTime;
			//System.out.println("Purge Time: " + purgeTime + " milliseconds");
			
			long endTime = System.currentTimeMillis();
			long purgeTime = (endTime - startTime) / (1000*60);
			
			System.out.println("Purge Time: " + purgeTime + " minutes");
			System.out.println("Document objects of class " + className + " deleted successfully.");
		} catch (EngineRuntimeException e) {
        	System.out.println("Domain connected: false");
        	e.printStackTrace();
       
        } finally {
			UserContext.get().popSubject();
			/*
			 * if (connection != null) { Factory.Connection.release(connection);
			 * }
			 */

		}
		} catch (Exception e) {
	    	System.out.println("Domain connected: false");
	    	e.printStackTrace();
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