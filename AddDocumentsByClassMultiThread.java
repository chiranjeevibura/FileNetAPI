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

public class AddDocumentsByClassMultiThread {
	public static void main(String[] args) {

		String username = " ";
		String password = " ";
		//UAT:// String url = "https:// /wsi/FNCEWS40MTOM/";
		String url = "https:// /wsi/FNCEWS40MTOM/";
		String documentClass = "P8_Test";
		//UAT String OSName = " ";
		String OSName = " ";
		//UAT String certificatePath = "C:\\Work\\fnuatc.crt";
		String certificatePath = "C:\\Work\\fndevc.crt";
		//String storagePolicyId = " ";
		int documentCount = 2; // Number of documents to be added
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
				// CompletionService<String> completionService = new
				// ExecutorCompletionService<>(executor);
				List<Future<String>> futures = new ArrayList<>();

				// Create the documents
				for (int i = 1; i <= documentCount; i++) {
					// Create a document instance
					Document document = Factory.Document.createInstance(objectStore, documentClass);

					// Set the document properties
					document.getProperties().putValue("DocumentTitle", "Document " + i);
					//document.getProperties().putValue("DocumentType", "16100");

					//StorageArea sa = Factory.StorageArea.getInstance(objectStore, new Id("{8830F810-0000-C18E-93EB-8A51C38D6641}") );
					//document.set_StorageArea(sa);
					
					//document.save(RefreshMode.NO_REFRESH);
					
					// Generate dynamic text file content with different sizes
					String content = generateDynamicTextContent(i, 20000, subject); // Adjust
																				// the
																				// multiplier
																				// to
																				// control
																				// the
																				// file
																				// size

					// Create content element
					ContentElementList contentElements = Factory.ContentElement.createList();
					InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
					ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
					contentTransfer.setCaptureSource(inputStream);
					contentElements.add(contentTransfer);

					// Set the content element on the document
					document.set_ContentElements(contentElements);
					
					document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION );

					// Create a DocumentUploader task and submit it to the
					// executor
					/*
					 * DocumentUploader uploader = new
					 * DocumentUploader(document);
					 * completionService.submit(uploader);
					 */
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

				// Wait for all tasks to complete and collect the results
				/*
				 * int completedTasks = 0; while (completedTasks <
				 * documentCount) { Future<String> resultFuture =
				 * completionService.take(); // Blocking call String result =
				 * resultFuture.get(); System.out.println(result);
				 * completedTasks++; }
				 */
				for (Future<String> future : futures) {
					try {
						String documentId = future.get();
						System.out.println("Document added: " + documentId);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

				// Write the output to a file
				writeOutputToFile(outputFilePath, documentIds, subject);

				// Shut down the executor
				// executor.shutdown();

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
			// Write the document IDs to the file
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
