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


public class FileNetAddFolder {
    public static void main(String[] args) {

        String username = " ";
        String password = " ";
        String url = "https:// /wsi/FNCEWS40MTOM/";
 
        String folderName1 = "testFolder ";
        String folderGUID1 = "{ - - - - }";
 
        
        // Create a connection to the FileNet domain
        try {
            // Load the SSL certificate
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream fileInputStream = new FileInputStream("C:\\Work\\fnuat.crt");
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
            ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, "ABGUATCS01", null);
            System.out.println("Object Store ="+ objectStore.get_DisplayName() );

            // Create root folder instance
            Folder testFolder1 = Factory.Folder.createInstance(objectStore, null);
            Folder rootFolder = objectStore.get_RootFolder();
            //System.out.println(rootFolder);
            testFolder1.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder1.getProperties().putValue("FolderName", folderName1);
            testFolder1.getProperties().putValue("Id", new Id(folderGUID1));
            testFolder1.save(RefreshMode.REFRESH);
            
       /*     //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.getProperties().putValue("Id", new Id(folderGUID2));
            subFolder.save(RefreshMode.REFRESH);*/

            // Print the folder information
            System.out.println("Folder added: " + testFolder1.get_Id().toString());
            System.out.println("Folder name: " + testFolder1.getProperties().getStringValue("FolderName"));
         
            
            // Create root folder instance
            Folder testFolder2 = Factory.Folder.createInstance(objectStore, null);
            testFolder2.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder2.getProperties().putValue("FolderName", folderName2);
            testFolder2.getProperties().putValue("Id", new Id(folderGUID2));
            testFolder2.save(RefreshMode.REFRESH);
            
       /*     //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.getProperties().putValue("Id", new Id(folderGUID2));
            subFolder.save(RefreshMode.REFRESH);*/

            // Print the folder information
            System.out.println("Folder added: " + testFolder2.get_Id().toString());
            System.out.println("Folder name: " + testFolder2.getProperties().getStringValue("FolderName"));
         
            
            // Create root folder instance
            Folder testFolder3 = Factory.Folder.createInstance(objectStore, null);
            testFolder3.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder3.getProperties().putValue("FolderName", folderName3);
            testFolder3.getProperties().putValue("Id", new Id(folderGUID3));
            testFolder3.save(RefreshMode.REFRESH);
            
       /*     //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.getProperties().putValue("Id", new Id(folderGUID2));
            subFolder.save(RefreshMode.REFRESH);*/

            // Print the folder information
            System.out.println("Folder added: " + testFolder3.get_Id().toString());
            System.out.println("Folder name: " + testFolder3.getProperties().getStringValue("FolderName"));
         
            
            // Create root folder instance
            Folder testFolder4 = Factory.Folder.createInstance(objectStore, null);
            testFolder4.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder4.getProperties().putValue("FolderName", folderName4);
            testFolder4.getProperties().putValue("Id", new Id(folderGUID4));
            testFolder4.save(RefreshMode.REFRESH);
            
       /*     //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.getProperties().putValue("Id", new Id(folderGUID2));
            subFolder.save(RefreshMode.REFRESH);*/

            // Print the folder information
            System.out.println("Folder added: " + testFolder4.get_Id().toString());
            System.out.println("Folder name: " + testFolder4.getProperties().getStringValue("FolderName"));
         
            
            // Create root folder instance
            Folder testFolder5 = Factory.Folder.createInstance(objectStore, null);
            testFolder5.set_Parent(rootFolder);

            
            // Set the folder properties
            testFolder5.getProperties().putValue("FolderName", folderName5);
            testFolder5.getProperties().putValue("Id", new Id(folderGUID5));
            testFolder5.save(RefreshMode.REFRESH);
            
       /*     //Creating Sub folder  
            com.filenet.api.core.Folder subFolder= testFolder.createSubFolder("newSubFolder");
            subFolder.getProperties().putValue("Id", new Id(folderGUID2));
            subFolder.save(RefreshMode.REFRESH);*/

            // Print the folder information
            System.out.println("Folder added: " + testFolder5.get_Id().toString());
            System.out.println("Folder name: " + testFolder5.getProperties().getStringValue("FolderName"));
         
            //System.out.println();
            
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