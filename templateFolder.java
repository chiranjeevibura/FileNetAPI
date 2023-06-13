import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.*;
import com.filenet.api.util.UserContext;
import com.filenet.api.exception.EngineRuntimeException;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class templateFolder {
    public static void main(String[] args) {

        String username = " ";
        String password = " ";
        String url = " ";
        //String rootFolderName = "Root Folder";
        String folderName = " ";
        String folderGUID = " ";
                	
        // Create a connection to the FileNet domain
        try {
            // Load the SSL certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream fileInputStream = new FileInputStream(" .crt");
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
            
            // Get the default domain
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            
            // Print the domain connection status
            System.out.println("Domain connected: True");
            System.out.println("Domain name: " + domain.get_Name());

            // Get the object store
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, " ", null);
            System.out.println("Object Store ="+ objectStore.get_DisplayName() );

            // Create root folder instance
            Folder testFolder = Factory.Folder.createInstance(objectStore, null);
            Folder rootFolder = objectStore.get_RootFolder();
            //System.out.println(rootFolder);
            testFolder.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder.getProperties().putValue("FolderName", folderName);
            testFolder.getProperties().putValue("Id", new Id(folderGUID));
            testFolder.save(RefreshMode.REFRESH);
            
            //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.save(RefreshMode.REFRESH);

            // Print the folder information
            System.out.println("Folder added: " + testFolder.get_Id().toString());
            System.out.println("Folder name: " + testFolder.getProperties().getStringValue("FolderName"));
         
            System.out.println();
            
            uc.popSubject();
        } catch (EngineRuntimeException e) {
        	System.out.println("Domain connected: false");
        	e.printStackTrace();
       
        }
    }catch (Exception e) {
    	System.out.println("Domain connected: false");
    	e.printStackTrace();
}
}

}