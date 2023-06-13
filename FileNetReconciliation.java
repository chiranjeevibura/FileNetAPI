import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.core.*;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.security.AccessPermission;

public class FileNetReconciliation {

	public static void main(String[] args) {

		String sourceUsername = " ";
		String sourcePassword = " ";
		String sourceURL = " ";

		String targetUsername = " ";
		String targetPassword = " ";
		String targetURL = " ";

		String sourceObjectStoreName = " ";
		String targetObjectStoreName = " ";
		
		String filePath = "C:\\Work\\ReconciliationReport.txt";

		// Create a connection to the source FileNet domain
		try {
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

			// Perform validations
			validateData(sourceObjectStore, targetObjectStore);
			validateMetadata(sourceObjectStore, targetObjectStore);
			performSearch(sourceObjectStore, targetObjectStore);
			validatePermissions(sourceObjectStore, targetObjectStore);

			// Generate reconciliation report
			generateReconciliationReport(filePath);

			// Pop the user context from the target domain
			targetUserContext.popSubject();
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void validateData(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
		System.out.println("Performing data validation...");

		// Create a search scope to search for documents in the source object
		// store
		SearchScope searchScope = new SearchScope(sourceObjectStore);

			
		// Create a searchSQL to retrieve all documents in the source object store
		
		String searchSQL = "SELECT * FROM P8_Recon_Test";

		// Create the search object using the search scope and search SQL
		SearchSQL search = new SearchSQL(searchSQL);
		RepositoryRowSet rowSet = searchScope.fetchRows(search, null, null, true);

		// Iterate through the search results and compare the document content
		// in the target object store
		Iterator<?> iterator = rowSet.iterator();
		while (iterator.hasNext()) {
			RepositoryRow row = (RepositoryRow) iterator.next();
			Properties properties = row.getProperties();
			//Id documentId = properties.getIdValue("Id");
			//Document document = Factory.Document.fetchInstance(objectStore, documentId, null);
			
			Document sourceDoc = (Document) row.getProperties().getObjectValue("This");
			//System.out.println("Checking document: " + documentId.toString());
			System.out.println("Data validation for document: " + sourceDoc.get_Id());
			// Check if the document exists in the target object store
            
			if (documentExistsInTarget(targetObjectStore, sourceDoc.get_Id())) {
                // Fetch the corresponding document from the target object store
                Document targetDoc = Factory.Document.fetchInstance(targetObjectStore, sourceDoc.get_Id(), null);

                // Compare the file content to validate the data integrity
                String sourceChecksum = generateChecksum(sourceDoc);
                String targetChecksum = generateChecksum(targetDoc);

                if (!sourceChecksum.equals(targetChecksum)) {
                    System.out.println("Data validation failed for document: " + sourceDoc.get_Id());
                    // Perform necessary actions for validation failure
                }
            } else {
                System.out.println("Document not found in the target object store: " + sourceDoc.get_Id());
                // Perform necessary actions when the document is not found in the target object store
            }
			
		}
	}
	
    private static boolean documentExistsInTarget(ObjectStore targetObjectStore, Id documentId) {
        try {
            // Fetch the document from the target object store
            Document targetDoc = Factory.Document.fetchInstance(targetObjectStore, documentId, null);
            return true;
        } catch (Exception e) {
            // Document not found
            return false;
        }
    }

	private static String generateChecksum(Document document) {
		// Read the content transfer of the document
		ContentElementList contentElements = document.get_ContentElements();
		if (contentElements != null && contentElements.size() > 0) {
			ContentTransfer contentTransfer = (ContentTransfer) contentElements.get(0);
			InputStream contentStream = contentTransfer.accessContentStream();

			try {
				// Generate the checksum for the content stream
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = contentStream.read(buffer)) != -1) {
					md.update(buffer, 0, bytesRead);
				}

				// Get the checksum value as a hexadecimal string
				byte[] checksumBytes = md.digest();
				StringBuilder sb = new StringBuilder();
				for (byte b : checksumBytes) {
					sb.append(String.format("%02x", b));
				}
				return sb.toString();
			} catch (NoSuchAlgorithmException | IOException e) {
				System.out.println("An error occurred while generating the checksum: " + e.getMessage());
				e.printStackTrace();
			} finally {
				if (contentStream != null) {
					try {
						contentStream.close();
					} catch (IOException e) {
						System.out.println("An error occurred while closing the content stream: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private static void validateMetadata(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
	    System.out.println("Performing metadata validation...");

	    // Prepare the search SQL query
	    String query = "SELECT DocumentTitle FROM P8_Recon_Test";
	    SearchSQL searchSQL = new SearchSQL(query);

	    // Set the search scope to the source object store
	    SearchScope searchScope = new SearchScope(sourceObjectStore);

	    // Execute the query to fetch the source documents
	    DocumentSet sourceDocuments = null;
	    try {
	        sourceDocuments = (DocumentSet) searchScope.fetchObjects(searchSQL, null, null, Boolean.FALSE);
	    } catch (Exception e) {
	        System.out.println("An error occurred while retrieving source documents: " + e.getMessage());
	        return;
	    }

	    // Iterate through the source documents and compare their metadata with
	    // their counterparts in the target object store
	    Iterator<?> iterator = sourceDocuments.iterator();
	    while (iterator.hasNext()) {
	        Document sourceDoc = (Document) iterator.next();
	        
	        // Fetch the corresponding document in the target object store
	        Document targetDoc = null;
	        try {
	            targetDoc = Factory.Document.fetchInstance(targetObjectStore, sourceDoc.get_Id(), null);
	        } catch (Exception e) {
	            System.out.println("An error occurred while fetching the target document: " + e.getMessage());
	            continue;
	        }

	        // Compare specific metadata fields to validate their values
	        // Example: Compare document titles
	        String sourceTitle = getPropertyValue(sourceDoc, "DocumentTitle");
	        String targetTitle = getPropertyValue(targetDoc, "DocumentTitle");
	        if (!sourceTitle.equals(targetTitle)) {
	            System.out.println("Metadata validation failed for document: " + sourceDoc.get_Id());
	            // Perform necessary actions for validation failure
	        }
	    }
	}

	private static String getPropertyValue(Document document, String propertyName) {
		Properties properties = document.getProperties();
		Property property = properties.get(propertyName);
		if (property != null) {
			return property.getStringValue();
		}
		return null;
	}

	private static void performSearch(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
		System.out.println("Performing search and retrieval validation...");

		// Example: Search for documents created by a specific user
		String query = "SELECT * FROM P8_Recon_Test WHERE Creator = 'p8padmin'";
		RepositoryRowSet sourceResults = executeQuery(sourceObjectStore, query);
		RepositoryRowSet targetResults = executeQuery(targetObjectStore, query);

		// Compare the search results to validate the search and retrieval
		// functionality
		compareSearchResults(sourceResults, targetResults);

	}
	
	private static void compareSearchResults(RepositoryRowSet sourceResults, RepositoryRowSet targetResults) {
	    if (sourceResults == null || targetResults == null) {
	        System.out.println("Search and retrieval validation failed: Null results");
	        return;
	    }

	    // Get the iterator for the source and target results
	    Iterator<RepositoryRow> sourceIterator = sourceResults.iterator();
	    Iterator<RepositoryRow> targetIterator = targetResults.iterator();

	    // Iterate over the search results and compare specific attributes
	    while (sourceIterator.hasNext() && targetIterator.hasNext()) {
	        RepositoryRow sourceRow = sourceIterator.next();
	        RepositoryRow targetRow = targetIterator.next();

	        // Example: Compare document titles
	        String sourceTitle = getPropertyValue(sourceRow, "DocumentTitle");
	        String targetTitle = getPropertyValue(targetRow, "DocumentTitle");

	        if (sourceTitle != null && targetTitle != null && !sourceTitle.equals(targetTitle)) {
	            System.out.println("Search and retrieval validation failed for document: " + getDocumentId(sourceRow));
	            // Perform necessary actions for validation failure
	        }
	    }
	}


	private static String getPropertyValue(RepositoryRow row, String propertyName) {
		// Retrieve the property value from the row
		// ...
		return null; // Replace with the actual property value retrieval logic
	}

	private static String getDocumentId(RepositoryRow row) {
		// Retrieve the document ID from the row
		// ...
		return null; // Replace with the actual document ID retrieval logic
	}

	private static RepositoryRowSet executeQuery(ObjectStore objectStore, String query) {
		SearchSQL searchSQL = new SearchSQL(query);
		SearchScope searchScope = new SearchScope(objectStore);
		RepositoryRowSet rowSet = searchScope.fetchRows(searchSQL, null, null, null);
		return rowSet;
	}

	private static void validatePermissions(ObjectStore sourceObjectStore, ObjectStore targetObjectStore) {
	    System.out.println("Performing permission and access control validation...");

	    // Prepare the search SQL query
	    String query = "SELECT * FROM P8_Recon_Test";
	    SearchSQL searchSQL = new SearchSQL(query);

	    // Set the search scope to the source object store
	    SearchScope searchScope = new SearchScope(sourceObjectStore);

	    // Execute the query to fetch the source documents
	    DocumentSet sourceDocuments = null;
	    try {
	        sourceDocuments = (DocumentSet) searchScope.fetchObjects(searchSQL, null, null, Boolean.FALSE);
	    } catch (Exception e) {
	        System.out.println("An error occurred while retrieving source documents: " + e.getMessage());
	        return;
	    }

	    // Iterate through the source documents and compare their access control
	    // lists (ACLs) with their counterparts in the target object store
	    Iterator<?> iterator = sourceDocuments.iterator();
	    while (iterator.hasNext()) {
	        Document sourceDoc = (Document) iterator.next();
	        
	        // Fetch the corresponding document in the target object store
	        Document targetDoc = null;
	        try {
	            targetDoc = Factory.Document.fetchInstance(targetObjectStore, sourceDoc.get_Id(), null);
	        } catch (Exception e) {
	            System.out.println("An error occurred while fetching the target document: " + e.getMessage());
	            continue;
	        }

	        // Compare the access control entries (ACEs) of the documents to
	        // validate the permissions
	        AccessPermissionList sourceACL = sourceDoc.get_Permissions();
	        AccessPermissionList targetACL = targetDoc.get_Permissions();

	        if (!compareAccessPermissions(sourceACL, targetACL)) {
	            System.out.println("Permission validation failed for document: " + sourceDoc.get_Id());
	            // Perform necessary actions for validation failure
	        }
	    }
	}

	private static boolean compareAccessPermissions(AccessPermissionList sourceACL, AccessPermissionList targetACL) {
		// Compare the number of ACEs
		if (sourceACL.size() != targetACL.size()) {
			return false;
		}

		// Compare each ACE individually
		Iterator<?> sourceIterator = sourceACL.iterator();
		Iterator<?> targetIterator = targetACL.iterator();
		while (sourceIterator.hasNext() && targetIterator.hasNext()) {
			AccessPermission sourceACE = (AccessPermission) sourceIterator.next();
			AccessPermission targetACE = (AccessPermission) targetIterator.next();

			// Compare ACE properties (e.g., grantee, access mask, permission
			// type, etc.)
			if (!compareACEProperties(sourceACE, targetACE)) {
				return false;
			}
		}

		return true;
	}

	private static boolean compareACEProperties(AccessPermission sourceACE, AccessPermission targetACE) {
		// Compare grantee, access mask, permission type, etc.
		// ...

		// Example: Compare the grantee name
		String sourceGrantee = sourceACE.get_GranteeName();
		String targetGrantee = targetACE.get_GranteeName();
		if (!sourceGrantee.equals(targetGrantee)) {
			return false;
		}

		// Compare other ACE properties
		// ...

		return true;
	}

	// Helper method to fetch objects from the object store
	private static RepositoryRowSet fetchObjects(ObjectStore objectStore, String query) {
		SearchSQL searchSQL = new SearchSQL(query);
		SearchScope searchScope = new SearchScope(objectStore);
		RepositoryRowSet rowSet = (RepositoryRowSet) searchScope.fetchRows(searchSQL, null, null, null);
		return rowSet;
	}

	// Other methods...
	private static void generateReconciliationReport(String filePath) {
	    System.out.println("Generating reconciliation report...");

	    // Open a file writer to write the report
	    try (FileWriter writer = new FileWriter(filePath)) {
	        // Write the report header
	        writer.write("Reconciliation Report\n");
	        writer.write("---------------------\n\n");

	        // Write the validation results
	        writer.write("Data Validation: ...\n");
	        writer.write("Metadata Validation: ...\n");
	        writer.write("Search and Retrieval Validation: ...\n");
	        writer.write("Permission and Access Control Validation: ...\n");

	        // Add more sections and details as needed

	        System.out.println("Reconciliation report generated successfully.");
	    } catch (IOException e) {
	        System.out.println("An error occurred while generating the reconciliation report: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

}
