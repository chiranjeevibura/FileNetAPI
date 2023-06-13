import com.filenet.api.collection.FolderSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class FileNet2Domains {

	public static void main(String[] args) {
		String sourceUsername = " ";
		String sourcePassword = " ";
 
		String sourceURL = "https:// /wsi/FNCEWS40MTOM/";
		String targetUsername = " ";
		String targetPassword = "   ";
 
		String targetURL = "https:// /wsi/FNCEWS40MTOM/";
 		String targetRootFolderName = " ";
 		String sourceFolderId = " ";
 

		String sourceCertificateName = "C:\\Work\\ .crt";
		String targetCertificateName = "C:\\Work\\ .crt";
 		String sourceObjectStoreName = " ";
		String targetObjectStoreName = " ";
 

		// Create a connection to the source FileNet domain
		try {
			disableSSLVerification();
			

			// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(sourceURL);
			UserContext sourceUserContext = UserContext.get();
			sourceUserContext.pushSubject(
					UserContext.createSubject(sourceConnection, sourceUsername, sourcePassword, "FileNetP8WSI"));

			// Get the source domain
			Domain sourceDomain = Factory.Domain.fetchInstance(sourceConnection, null, null);

			// Print the source domain connection status
			System.out.println("Source Domain connected: True");
			System.out.println("Source Domain name: " + sourceDomain.get_Name());

			// Get the source object store
			ObjectStore sourceObjectStore = Factory.ObjectStore.fetchInstance(sourceDomain, sourceObjectStoreName,
					null);
			System.out.println("Source Object Store: " + sourceObjectStore.get_DisplayName());

			// Fetch the source folder from the source object store
			Folder sourceFolder = Factory.Folder.fetchInstance(sourceObjectStore, sourceFolderId, null);
			// Folder sourceFolder = Factory.Folder.
			if (sourceFolder == null) {
				System.out.println("Source folder not found");
				return;
			}
			System.out.println("Source Folder: " + sourceFolder.get_FolderName());
			System.out.println("Source Folder ID: " + sourceFolder.get_Id().toString());
			

			// Connect to the target domain
			Connection targetConnection = Factory.Connection.getConnection(targetURL);
			UserContext targetUserContext = UserContext.get();
			targetUserContext.pushSubject(
					UserContext.createSubject(targetConnection, targetUsername, targetPassword, "FileNetP8WSI"));

			// Get the target domain
			Domain targetDomain = Factory.Domain.fetchInstance(targetConnection, null, null);

			// Print the target domain connection status
			System.out.println("Target Domain connected: True");
			System.out.println("Target Domain name: " + targetDomain.get_Name());

			// Get the target object store
			ObjectStore targetObjectStore = Factory.ObjectStore.fetchInstance(targetDomain, targetObjectStoreName,
					null);
			System.out.println("Target Object Store: " + targetObjectStore.get_DisplayName());

			// Create the target root folder instance
			// Folder targetRootFolder =
			// Factory.Folder.fetchInstance(targetObjectStore,
			// targetRootFolderName, null);

			// Create root folder instance
			Folder targetFolder = targetObjectStore.get_RootFolder();

			// Folder targetFolder =
			// Factory.Folder.createInstance(targetObjectStore, null);

			// Folder rootFolder = targetObjectStore.get_RootFolder();
			// System.out.println(rootFolder);
			// targetFolder.set_Parent(rootFolder);

			// Set the folder properties
			// targetFolder.getProperties().putValue("FolderName",
			// sourceFolder.get_FolderName());
			// targetFolder.getProperties().putValue("Id", new
			// Id(sourceFolder.get_Id().toString()));

			// Save the target root folder
			// targetFolder.save(RefreshMode.REFRESH);
			System.out.println("Target Folder assigned successfully");
			// System.out.println("Target Folder created successfully");
			System.out.println("Target Folder ID: " + targetFolder.get_Id().toString());
			/*
			 * // Set the folder properties
			 * targetRootFolder.getProperties().putValue("FolderName",
			 * targetRootFolderName);
			 * targetRootFolder.getProperties().putValue("Id", new
			 * Id(sourceFolderId)); targetRootFolder.save(RefreshMode.REFRESH);
			 */
			// Creating Sub folder
			// Folder subFolder=
			// targetRootFolder.createSubFolder("newSubFolder");
			// subFolder.save(RefreshMode.REFRESH);

			// Copy the source folder to the target root folder recursively
			//copyFolder(sourceFolder, targetFolder);

			// System.out.println("Copy completed successfully.");

			// Pop the user context from the target domain
			targetUserContext.popSubject();
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void disableSSLVerification() {
		try {
			// Create a trust manager that accepts all certificates
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Create an SSL context with the custom trust manager
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

			// Set the default SSL socket factory
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

			// Set the default hostname verifier
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void copyFolder(Folder sourceFolder, Folder targetFolder) {
		try {
			// Copy the source folder to the target folder
			Folder targetSubfolder = getOrCreateTargetFolder(sourceFolder, targetFolder);
			if (targetSubfolder == null) {
				System.out.println("Sub Folder under Target Folder not exist.");
				return;
			}

			// Copy the associated objects of the source folder to the target
			// folder
			Iterator<?> iterator = sourceFolder.get_ContainedDocuments().iterator();
			/*
			 * while (iterator.hasNext()) { Document sourceDocument = (Document)
			 * iterator.next(); copyDocument(sourceDocument, targetSubfolder); }
			 */
			// Recursively copy the subfolders of the source folder to the
			// target folder
			iterator = sourceFolder.get_SubFolders().iterator();
			while (iterator.hasNext()) {
				Folder sourceSubfolder = (Folder) iterator.next();
				copyFolder(sourceSubfolder, targetSubfolder);
			}
		} catch (Exception e) {
			System.out.println("An error occurred while copying folder: " + e.getMessage());
			e.printStackTrace();
		}
	}


	private static Folder getOrCreateTargetFolder(Folder sourceFolder, Folder targetFolder) throws Exception {
		try {
			// Check if the target folder exists
			FolderSet folderSet = targetFolder.get_SubFolders();
			Iterator<?> iterator = folderSet.iterator();
			while (iterator.hasNext()) {
				Folder existingFolder = (Folder) iterator.next();
				if (existingFolder.get_FolderName().equals(sourceFolder.get_FolderName())) {
					return existingFolder;
				}
			}

			// Create the target subfolder instance
			Folder targetSubfolder = Factory.Folder.createInstance(targetFolder.getObjectStore(), null);
			targetSubfolder.set_Parent(targetFolder);
			targetSubfolder.set_FolderName(sourceFolder.get_FolderName());
			targetSubfolder.getProperties().putValue("Id", new Id(sourceFolder.get_Id().toString()));
			targetSubfolder.save(RefreshMode.REFRESH);
			System.out.println("Source SubFolder ID: " + sourceFolder.get_Id().toString());

			System.out.println("Copied Folder: " + targetSubfolder.get_FolderName());
			System.out.println("Copied Folder ID: " + targetSubfolder.get_Id().toString());

			return targetSubfolder;
		} catch (Exception e) {
			System.out.println("An error occurred while creating target folder: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
