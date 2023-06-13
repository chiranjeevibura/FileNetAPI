import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class FileNetFolderCopy {
    public static void main(String[] args) {
        // Source connection details
        String sourceUsername = " ";
        String sourcePassword = " ";
        String sourceUrl = " ";
        String sourceCertificatePath = "source_certificate.crt";

        // Target connection details
        String targetUsername = " ";
        String targetPassword = " ";
        String targetUrl = " ";
        String targetCertificatePath = "target_certificate.crt";

        String folderName = " "; // Specify the folder name to copy
        String folderGUID = " "; // Specify the folder GUID of the root folder

        // Create a connection to the FileNet source domain
        try {
            // Load the source SSL certificate
            CertificateFactory sourceCertificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream sourceCertificateInputStream = new FileInputStream(sourceCertificatePath);
            X509Certificate sourceCertificate = (X509Certificate) sourceCertificateFactory.generateCertificate(sourceCertificateInputStream);
            sourceCertificateInputStream.close();

            // Create a KeyStore for the source certificate
            KeyStore sourceKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            sourceKeyStore.load(null, null);
            sourceKeyStore.setCertificateEntry("sourceNetCertificate", sourceCertificate);

            // Create a TrustManager that trusts the source certificate
            TrustManagerFactory sourceTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            sourceTrustManagerFactory.init(sourceKeyStore);

            // Create an SSL context with the TrustManager for the source connection
            SSLContext sourceSslContext = SSLContext.getInstance("TLS");
            sourceSslContext.init(null, sourceTrustManagerFactory.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sourceSslContext.getSocketFactory());

            // Create the source connection
            Connection sourceConnection = Factory.Connection.getConnection(sourceUrl);
            UserContext sourceUserContext = UserContext.get();
            sourceUserContext.pushSubject(UserContext.createSubject(sourceConnection, sourceUsername, sourcePassword, "FileNetP8WSI"));

            // Get the source domain
            Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

            // Print the source domain connection status
            System.out.println("Source domain connected: True");
            System.out.println("Source domain name: " + sourceDomain.get_Name());

            // Get the source object store
            ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, " ", null);
            System.out.println("Source object store: " + sourceObjectStore.get_DisplayName());

            // Create the root folder instance in the source object store
            Folder sourceRootFolder = Factory.Folder.fetchInstance(sourceObjectStore, new Id(folderGUID), null);
            System.out.println("Source root folder: " + sourceRootFolder.get_PathName());

            // Create a connection to the FileNet target domain
            // Load the target SSL certificate
            CertificateFactory targetCertificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream targetCertificateInputStream = new FileInputStream(targetCertificatePath);
            X509Certificate targetCertificate = (X509Certificate) targetCertificateFactory.generateCertificate(targetCertificateInputStream);
            targetCertificateInputStream.close();

            // Create a KeyStore for the target certificate
            KeyStore targetKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            targetKeyStore.load(null, null);
            targetKeyStore.setCertificateEntry("targetNetCertificate", targetCertificate);

            // Create a TrustManager that trusts the target certificate
            TrustManagerFactory targetTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            targetTrustManagerFactory.init(targetKeyStore);

            // Create an SSL context with the TrustManager for the target connection
            SSLContext targetSslContext = SSLContext.getInstance("TLS");
            targetSslContext.init(null, targetTrustManagerFactory.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(targetSslContext.getSocketFactory());

            // Create the target connection
            Connection targetConnection = Factory.Connection.getConnection(targetUrl);
            UserContext targetUserContext = UserContext.get();
            targetUserContext.pushSubject(UserContext.createSubject(targetConnection, targetUsername, targetPassword, "FileNetP8WSI"));

            // Get the target domain
            Domain targetDomain = Factory.Domain.fetchInstance(targetConnection, null, null);

            // Print the target domain connection status
            System.out.println("Target domain connected: True");
            System.out.println("Target domain name: " + targetDomain.get_Name());

            // Get the target object store
            ObjectStore targetObjectStore = Factory.ObjectStore.fetchInstance(targetDomain, " ", null);
            System.out.println("Target object store: " + targetObjectStore.get_DisplayName());

            // Create the target root folder instance in the target object store
            Folder targetRootFolder = Factory.Folder.fetchInstance(targetObjectStore, new Id(folderGUID), null);
            System.out.println("Target root folder: " + targetRootFolder.get_PathName());

            // Create the target folder and subfolders
            Folder targetFolder = Factory.Folder.createInstance(targetObjectStore, null);
            targetFolder.set_Parent(targetRootFolder);
            targetFolder.getProperties().putValue("FolderName", folderName);
            targetFolder.getProperties().putValue("Id", new Id(folderGUID));
            targetFolder.save(RefreshMode.REFRESH);
            System.out.println("Target folder created: " + targetFolder.get_Id().toString());

            // Copy subfolders and associated objects from source to target
            copySubfoldersAndObjects(sourceRootFolder, targetFolder, sourceObjectStore, targetObjectStore);

            // Pop the subjects from source and target connections
            sourceUserContext.popSubject();
            targetUserContext.popSubject();

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Recursively copy subfolders and associated objects from source to target
    private static void copySubfoldersAndObjects(Folder sourceFolder, Folder targetFolder, ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
        try {
            // Copy objects associated with the source folder
            for (ReferentialContainmentRelationship rcr : sourceFolder.get_ContainedDocuments()) {
                Document sourceDocument = (Document) rcr.get_ContainedObject();
                Document targetDocument = (Document) sourceDocument.copy(targetFolder, null);
                targetDocument.save(RefreshMode.REFRESH);
                System.out.println("Document copied: " + targetDocument.get_Id().toString());
            }

            for (ReferentialContainmentRelationship rcr : sourceFolder.get_ContainedFolders()) {
                Folder sourceSubfolder = (Folder) rcr.get_ContainedObject();
                Folder targetSubfolder = Factory.Folder.createInstance(targetObjectStore, null);
                targetSubfolder.set_Parent(targetFolder);
                targetSubfolder.set_FolderName(sourceSubfolder.get_FolderName());
                targetSubfolder.save(RefreshMode.REFRESH);
                System.out.println("Subfolder created: " + targetSubfolder.get_Id().toString());

                // Recursively copy subfolders and associated objects
                copySubfoldersAndObjects(sourceSubfolder, targetSubfolder, sourceObjectStore, targetObjectStore);
            }

        } catch (Exception e) {
            System.out.println("An error occurred during folder copy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
