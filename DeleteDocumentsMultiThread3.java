import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DeleteDocumentsMultiThread3 {
    private static final String FILENET_URL = "https:// /wsi/FNCEWS40MTOM/";
    private static final String USERNAME = " ";
    private static final String PASSWORD = " ";
    private static final String OBJECT_STORE_NAME = " ";
    private static final String DOCUMENT_CLASS = "P8_Test";
    private static final int BATCH_SIZE = 100;
    private static final int NUM_THREADS = 5;
    private static final int NUM_PROCESSES = 2;

    public static void main(String[] args) {
        try {
            // Set up SSL communication
            setupSSLCommunication();

            // Create a connection to the FileNet domain
            Connection connection = Factory.Connection.getConnection(FILENET_URL);
            Subject subject = UserContext.createSubject(connection, USERNAME, PASSWORD, null);
            UserContext.get().pushSubject(subject);

            // Get the default domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, OBJECT_STORE_NAME, null);

            // Get the document class
            ClassDefinition documentClass = Factory.ClassDefinition.fetchInstance(objectStore, DOCUMENT_CLASS, null);

            // Fetch the initial count of documents in the class
            int beforeCount = getDocumentCount(objectStore, documentClass);

            System.out.println("Before Count: " + beforeCount);

            // Create the executor service with a fixed number of threads
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

            // Create multiple processes
            ExecutorService processExecutor = Executors.newFixedThreadPool(NUM_PROCESSES);

            // Divide the total batch size among the processes
            int batchSizePerProcess = BATCH_SIZE / NUM_PROCESSES;

            // Start time
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < NUM_PROCESSES; i++) {
                final int processIndex = i;
                processExecutor.submit(() -> {
                    int startIndex = processIndex * batchSizePerProcess;

                    // Fetch and delete documents in parallel threads
                    boolean moreDocuments = true;

                    while (moreDocuments) {
                        // Fetch a batch of documents
                        RepositoryRowSet rowSet = fetchDocuments(objectStore, documentClass, startIndex, batchSizePerProcess);
                        Iterator iterator = rowSet.iterator();
                        // Check if there are more documents to process
                        moreDocuments = iterator.hasNext();

 /*           			for (Iterator it = rowSet.iterator(); it.hasNext();) {
            				RepositoryRow row = (RepositoryRow) it.next();
            				Properties properties = row.getProperties();
            				Id documentId = properties.getIdValue("Id");

            				// Creation a deletion task and submit it to the executor
            				Runnable deletionTask = createDeletionTask(objectStore, documentId, subject);
            				executor.submit(deletionTask);
            			}*/
                        // Process the documents in parallel threads
                        while (iterator.hasNext()) {
                            RepositoryRow row = (RepositoryRow) iterator.next();
                            //Document document = (Document) row.get_Id().get_IdValue();
            				Properties properties = row.getProperties();
            				Id documentId = properties.getIdValue("Id");

                            // Submit the deletion task to the executor service
                            executor.submit(() -> deleteDocument(objectStore, documentId));
                        }

                        // Update the start index for the next batch
                        startIndex += (NUM_PROCESSES * batchSizePerProcess);
                    }
                });
            }

            // Shut down the process executor service
            processExecutor.shutdown();

            // Wait for all processes to complete
            processExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Shut down the executor service
            executor.shutdown();

            // Wait for all threads to complete
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Fetch the count of documents after deletion
            int afterCount = getDocumentCount(objectStore, documentClass);

            System.out.println("After Count: " + afterCount);

            // Calculate the total deletion count
            int deletionCount = beforeCount - afterCount;

            System.out.println("Total Deletion Count: " + deletionCount);

            UserContext.get().popSubject();

            // Calculate and print the purge time in minutes
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            long purgeTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
            System.out.println("Purge Time: " + purgeTimeMinutes + " minutes");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteDocument(ObjectStore objectStore,Id documentId) {
        try {
        	Document document = Factory.Document.fetchInstance(objectStore, documentId, null);
            document.delete();
			document.save(RefreshMode.REFRESH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RepositoryRowSet fetchDocuments(ObjectStore objectStore, ClassDefinition documentClass, int startIndex, int batchSize) {
        PropertyFilter propertyFilter = createPropertyFilter();
        return documentClass.get_InstancesToFetch(startIndex, batchSize, null, propertyFilter);
    }

    private static PropertyFilter createPropertyFilter() {
        PropertyFilter propertyFilter = new PropertyFilter();
        propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null));
        return propertyFilter;
    }

    private static int getDocumentCount(ObjectStore objectStore, ClassDefinition documentClass) {
        PropertyFilter propertyFilter = createPropertyFilter();
        return documentClass.
        		countInstances(propertyFilter);
    }

    private static void setupSSLCommunication() {
        try {
            // Load the SSL certificate
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null); // No password required

            // Create the TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // Create the SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Set the default SSLSocketFactory for HTTPS connections
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
