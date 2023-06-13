import com.filenet.api.admin.StorageArea;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.core.Factory.ClassDefinition;
import com.filenet.api.core.Factory.DocumentClassDefinition;
import com.filenet.api.core.Factory.StoragePolicy;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.filenet.api.exception.EngineRuntimeException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.concurrent.*;

public class AddDocumentsByClassMultiThread2 {
    public static void main(String[] args) {
		String username = " ";
		String password = " ";
 
		String url = "https:// /wsi/FNCEWS40MTOM/";
		String certificatePath = "C:\\Work\\fnuat.crt";
		String documentClass = "P8_Recon_Test";
 
		String OSName = " ";
 
		int documentCount = 10; // Number of documents to be added
		int threadCount = 1; // Number of threads for multi-threading
		String outputFilePath = "C:\\Work\\output.txt"; // Output file path
        List<String> documentIds = new ArrayList<>();

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
                // Establish the user context
                Subject subject = UserContext.createSubject(connection, username, password, null);
                UserContext.get().pushSubject(subject);

                // Get the default domain
                Domain domain = Factory.Domain.fetchInstance(connection, null, null);

                // Print the domain connection status
                System.out.println("Domain connected: true");
                System.out.println("Domain name: " + domain.get_Name());

                // Get the object store
                ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, OSName, null);

                // Create a thread pool
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);

                // Create a list to store the Future results
                List<Future<String>> futures = new ArrayList<>();

                // Start the timer
                long startTime = System.currentTimeMillis();

                // Create the documents
                for (int i = 1; i <= documentCount; i++) {
                    Document document = Factory.Document.createInstance(objectStore, documentClass);
                    document.getProperties().putValue("DocumentTitle", "Document " + i);
                   // document.getProperties().putValue("DocumentType", "16100");
                    
                    String content = generateDynamicTextContent(i, 1000000, subject);
                    ContentElementList contentElements = Factory.ContentElement.createList();
                    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                    ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
                    contentTransfer.setCaptureSource(inputStream);
                    contentElements.add(contentTransfer);
                    document.set_ContentElements(contentElements);

					document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION );

                    Callable<String> documentCreationTask = () -> {
                        UserContext.get().pushSubject(subject);
                        document.save(RefreshMode.REFRESH);
                        String documentId = document.get_Id().toString();
                        documentIds.add(documentId);
                        return documentId;
                    };

                    Future<String> future = executor.submit(documentCreationTask);
                    futures.add(future);
                }

				for (Future<String> future : futures) {
					try {
						String documentId = future.get();
						System.out.println("Document added: " + documentId);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

                // Wait for all tasks to complete
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

                // Stop the timer
                long endTime = System.currentTimeMillis();

                // Print the document count and time taken
                System.out.println("Total documents added: " + documentCount);
                System.out.println("Time taken: " + (endTime - startTime) / (1000 * 60) + " minutes");

                // Write the output to a file
                writeOutputToFile(outputFilePath, documentIds, subject);

                UserContext.get().popSubject();
            } catch (EngineRuntimeException e) {
                System.out.println("Domain connected: false");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Domain connected: false");
            e.printStackTrace();
        }
    }

    private static String generateDynamicTextContent(int documentNumber, int fileSizeInBytes, Subject subject) {
        UserContext.get().pushSubject(subject);
        StringBuilder contentBuilder = new StringBuilder();
        int currentSize = 0;
        String line = "This is the content of Document " + documentNumber;
        while (currentSize + line.length() < fileSizeInBytes) {
            contentBuilder.append(line).append(System.lineSeparator());
            currentSize += line.length() + System.lineSeparator().length();
        }
        return contentBuilder.toString();
    }

    private static void writeOutputToFile(String filePath, List<String> documentIds, Subject subject) {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            UserContext.get().pushSubject(subject);
            writer.println("DocumentIDs: ");
            for (String documentId : documentIds) {
                writer.println(documentId);
            }
            System.out.println("Output written to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
