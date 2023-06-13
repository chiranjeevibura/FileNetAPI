import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.*;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class AddDocumentsByClass {
    public static void main(String[] args) {

        String username = " ";
        String password = " ";
         String documentClass = "P8_Test";
		String url = "https:// /wsi/FNCEWS40MTOM/";
		String certificatePath = "C:\\Work\\fnuat.crt";
        int documentCount = 1;
        // String cetrtifiactePath = "C:\\Work\\fnuatc.crt"
        	
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
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // Create an SSL context with the TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            Connection connection = Factory.Connection.getConnection(url);
            try {
        	UserContext uc =UserContext.get();
        	uc.pushSubject(UserContext.createSubject(connection, username, password, "FileNetP8WSI" ));
            
            //UserContext.get().pushSubject(subject);

            // Get the default domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            
            
            // Print the domain connection status
            System.out.println("Domain connected: true");
            System.out.println("Domain name: " + domain.get_Name());

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, "ABGUATCS01", null);
            String id1 = "{887E2D40-0000-CA55-A832-198CB1EE35EE}";
            //String Title1 = "Document ";
            // Create the documents
            for (int i = 1; i <= documentCount; i++) {
                // Create a document instance
                Document document = Factory.Document.createInstance(objectStore, documentClass);

                // Set the document properties
               //putValue("Id", new Id(sourceFolder.get_Id().toString()));
                document.getProperties().putValue("Id", new Id(id1));
                document.getProperties().putValue("DocumentTitle", "Document 10");
                //document.getProperties().putValue("DocumentType", "16100");
                
                // Generate dynamic text file content
                String content = generateDynamicTextContent(i);

                // Create content element
                ContentElementList contentElements = Factory.ContentElement.createList();
                InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
                contentTransfer.setCaptureSource(inputStream);
                contentElements.add(contentTransfer);

                // Set the content element on the document
                document.set_ContentElements(contentElements);

                // Save the document
                document.save(RefreshMode.REFRESH);

                // Print the document information
                System.out.println("Document added: " + document.get_Id().toString());
                System.out.println("Document title: " + document.getProperties().getStringValue("DocumentTitle"));
                System.out.println("Document class: " + document.getClassName());
                System.out.println();
            }

            System.out.println("Document class: " + documentClass + " >> " + "Total " + documentCount + " number of documents got added successfully");
        
            
            uc.popSubject();
            //UserContext.get().popSubject();
            //deleteDocumentObjects(connection, className);
        } catch (EngineRuntimeException e) {
        	System.out.println("Domain connected: false");
        	e.printStackTrace();
       
        }
    }catch (Exception e) {
    	System.out.println("Domain connected: false");
    	e.printStackTrace();
}
}
    private static String generateDynamicTextContent(int documentNumber) {
        return "This is the content of Document " + documentNumber;
    }

}