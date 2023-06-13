import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.filenet.api.core.*;
import com.filenet.api.admin.*;
import com.filenet.api.collection.*;
import com.filenet.api.constants.*;
import com.filenet.api.util.*;
import com.filenet.api.property.*;

public class PropertyTemplateSync2 {

	public static void main(String[] args) {
		String sourceUri = "https:// /wsi/FNCEWS40MTOM/";
		String sourceUsername = " ";
		String sourcePassword = "   ";
		// String sourceDomain = "source-domain";
		String sourceObjectStoreName = " ";

		String targetUri = "https:// /wsi/FNCEWS40MTOM/";
		String targetUsername = " ";
		String targetPassword = " ";
		// String targetDomain = "target-domain";
		String targetObjectStoreName = " ";

		try {
			disableSSLVerification();
			/*
			 * // Connect to the source FileNet domain Connection
			 * sourceConnection = Factory.Connection.getConnection(sourceUri);
			 * Subject sourceSubject =
			 * UserContext.createSubject(sourceConnection, sourceUsername,
			 * sourcePassword, sourceDomain);
			 * UserContext.get().pushSubject(sourceSubject); Domain
			 * sourceDomainObj = Factory.Domain.fetchInstance(sourceConnection,
			 * null, null);
			 */

			// Connect to the source domain
			Connection sourceConnection = Factory.Connection.getConnection(sourceUri);
			UserContext sourceSubject = UserContext.get();
			sourceSubject.pushSubject(
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

			/*
			 * // Connect to the target FileNet domain Connection
			 * targetConnection = Factory.Connection.getConnection(targetUri);
			 * Subject targetSubject =
			 * UserContext.createSubject(targetConnection, targetUsername,
			 * targetPassword, targetDomain);
			 * UserContext.get().pushSubject(targetSubject); Domain
			 * targetDomainObj = Factory.Domain.fetchInstance(targetConnection,
			 * null, null);
			 */

			// Connect to the target domain
			Connection targetConnection = Factory.Connection.getConnection(targetUri);
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
			
			// Retrieve property templates from source and target
            List<String> sourcePropertyTemplates = getSourcePropertyTemplates(sourceDomain, sourceObjectStore);
            System.out.println("Property Templates in Object Store: " + sourceObjectStore.get_DisplayName() + ": "+ sourcePropertyTemplates);
            List<String> targetPropertyTemplates = getTargetPropertyTemplates(targetDomain, targetObjectStore);
            System.out.println("Property Templates in Object Store: " + targetObjectStore.get_DisplayName() + ": "+ targetPropertyTemplates);

			// Compare and list missing property templates
            int missingCount = 0;
			List<String> missingPropertyTemplates = new ArrayList<>();
			for (String template : sourcePropertyTemplates) {
				if (!targetPropertyTemplates.contains(template)) {
					missingPropertyTemplates.add(template);
					missingCount++;
				}
			}

			// Add missing property templates to target
			/*if (!missingPropertyTemplates.isEmpty()) {
				addObjectStorePropertyTemplates(targetDomain, targetObjectStore, missingPropertyTemplates);
			}*/

			// Generate a report
			generateReport(missingPropertyTemplates, missingCount);

			// Cleanup and close connections
			UserContext.get().popSubject();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<String> getSourcePropertyTemplates(Domain domain, ObjectStore sourceObjectStore) {
		List<String> propertyTemplates = new ArrayList<>();
		try {
			//ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, sourceObjectStore, null);
			PropertyFilter propertyFilter = new PropertyFilter();
			// propertyFilter.addIncludeProperty(new FilterElement(null, null,
			// null, PropertyNames.PROMPT, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.SYMBOLIC_NAME, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DISPLAY_NAME, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DESCRIPTION, null));
			//propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DATATYPE, null));

			//Folder folder = Factory.Folder.fetchInstance(sourceObjectStore, "Property Templates", null);
			PropertyTemplateSet propertySet = sourceObjectStore.get_PropertyTemplates();
			// Retrieve the property templates using Java's Iterator
			int count = 0;
	        Iterator templateIterator = propertySet.iterator();
	        while (templateIterator.hasNext()) {
	        	PropertyTemplate ptml = (PropertyTemplate) templateIterator.next();
	        	propertyTemplates.add(ptml.get_SymbolicName());
	            //System.out.println("Property Template in Object Store: "+ sourceObjectStore.get_DisplayName() + ":" +ptml.get_SymbolicName());
	    	count++;      
	        }
	        System.out.println("Count of Property Template in Object Store: "+ sourceObjectStore.get_DisplayName() + ":" +count);
	        
		} catch (Exception e) {
			e.printStackTrace();
		};
		return propertyTemplates;
	}

	private static List<String> getTargetPropertyTemplates(Domain domain, ObjectStore targetObjectStore) {
		List<String> propertyTemplates = new ArrayList<>();
		try {
			//ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, sourceObjectStore, null);
			PropertyFilter propertyFilter = new PropertyFilter();
			// propertyFilter.addIncludeProperty(new FilterElement(null, null,
			// null, PropertyNames.PROMPT, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.SYMBOLIC_NAME, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DISPLAY_NAME, null));
			propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DESCRIPTION, null));
			//propertyFilter.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.DATATYPE, null));

			//Folder folder = Factory.Folder.fetchInstance(sourceObjectStore, "Property Templates", null);
			PropertyTemplateSet propertySet = targetObjectStore.get_PropertyTemplates();
			// Retrieve the property templates using Java's Iterator
	       int count =0;
			Iterator templateIterator = propertySet.iterator();
	        while (templateIterator.hasNext()) {
	        	PropertyTemplate ptml = (PropertyTemplate) templateIterator.next();
	        	propertyTemplates.add(ptml.get_SymbolicName());
	            //System.out.println("Property Template in Object Store: "+ targetObjectStore.get_DisplayName() + ":" +ptml.get_SymbolicName());
	    	count++;      
	        }
	        System.out.println("Count of Property Template in Object Store: "+ targetObjectStore.get_DisplayName() + ":" +count);

		} catch (Exception e) {
			e.printStackTrace();
		};
		return propertyTemplates;
	}
	private static void addObjectStorePropertyTemplates(Domain domain, ObjectStore sourceObjectStore,
			List<String> propertyTemplates) {
		try {
			//ObjectStore objectStore = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
			//Folder folder = Factory.Folder.fetchInstance(sourceObjectStore, "/PropertyTemplates", null);
			PropertyTemplateSet templateSet = sourceObjectStore.get_PropertyTemplates();
			for (String templateName : propertyTemplates) {
				PropertyTemplate template = (PropertyTemplate) Factory.PropertyTemplate.getInstance(sourceObjectStore, null);
				((ObjectStore) template).set_SymbolicName(templateName);
				// Set additional properties for the template as needed
				((List<String>) templateSet).addAll((Collection<? extends String>) template);
			}
			((IndependentlyPersistableObject) templateSet).save(RefreshMode.REFRESH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateReport(List<String> missingPropertyTemplates, int missingCount) {
		System.out.println("Missing Property Templates Count is: " + missingCount);
		System.out.println("Missing Property Templates:");
		for (String template : missingPropertyTemplates) {
			System.out.println(template);
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
}
